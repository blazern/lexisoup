#!/usr/bin/env bash
set -euo pipefail

usage() {
    cat <<'EOF'
Usage: kmp-create-module --type core|library|feature --package PACKAGE

Example:
  ./bash/kmp-create-module --type library --package blazern.lexisoup.data.translator.example

Packages must start with blazern.lexisoup.{core,data,domain,feature}.
Dots become directories; underscores become hyphens in the module path.
The current template contents are copied, excluding build and local caches.
Requires Bash, rsync and Perl (available on macOS and most Linux setups).
EOF
}

die() { printf 'Error: %s\n' "$*" >&2; exit 1; }

module_type=
package=
while [[ $# -gt 0 ]]; do
    case "$1" in
        --type)
            [[ $# -ge 2 && -z "$module_type" ]] || die '--type requires one value and may only appear once.'
            module_type=$2; shift 2 ;;
        --package)
            [[ $# -ge 2 && -z "$package" ]] || die '--package requires one value and may only appear once.'
            package=$2; shift 2 ;;
        -h|--help) usage; exit 0 ;;
        *) die "Unknown argument: $1 (see --help)" ;;
    esac
done

case "$module_type" in core|library|feature) ;; *) die '--type must be core, library or feature.' ;; esac
[[ "$package" =~ ^blazern\.lexisoup\.(core|data|domain|feature)(\.[a-z][a-z0-9_]*)+$ ]] ||
    die '--package must contain a layer and module name, using lowercase identifiers.'
# Unescaped Kotlin keywords cannot be used as package segments.
IFS='.' read -r -a segments <<< "$package"
for segment in "${segments[@]}"; do
    case "$segment" in
        as|break|class|continue|do|else|false|for|fun|if|in|interface|is|null|object|package|return|super|this|throw|true|try|typealias|typeof|val|var|when|while)
            die "Kotlin keyword in package: $segment" ;;
    esac
done
for tool in rsync perl; do command -v "$tool" >/dev/null || die "Required command not found: $tool"; done

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
project_root=$(cd -- "$script_dir/../lexisoup-kmp" && pwd)
template="$project_root/a-template-kmp-common"
settings="$project_root/settings.gradle.kts"
relative=${package#blazern.lexisoup.}
relative=${relative//./\/}
relative=${relative//_/-}
module_path=":${relative//\//:}"
destination="$project_root/$relative"
[[ -f "$template/build.gradle.kts" && -f "$settings" ]] || die 'Template or settings.gradle.kts missing.'
[[ ! -e "$destination" && ! -L "$destination" ]] || die "Destination already exists: $destination"
if grep -Fq "\"$module_path\"" "$settings"; then die "Module already registered: $module_path"; fi

# Reject symlinked parent directories rather than writing outside the project.
parent=$(dirname -- "$destination")
ancestor=$parent
while [[ "$ancestor" != "$project_root" ]]; do
    [[ ! -L "$ancestor" ]] || die "Symlinked module parent: $ancestor"
    [[ ! -e "$ancestor" || -d "$ancestor" ]] || die "Module parent is not a directory: $ancestor"
    ancestor=$(dirname -- "$ancestor")
done

stage=$(mktemp -d "$project_root/.kmp-create-module.XXXXXX")
installed=false
committed=false
created_parents=()
cleanup() {
    if [[ "$committed" != true ]]; then
        if [[ "$installed" == true ]]; then rm -rf -- "$destination"; fi
        for directory in "${created_parents[@]+"${created_parents[@]}"}"; do rmdir -- "$directory" 2>/dev/null || true; done
    fi
    rm -rf -- "$stage"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

mkdir "$stage/module"
rsync -a --exclude='build/' --exclude='.gradle/' --exclude='.kotlin/' \
    --exclude='.idea/' --exclude='.git/' --exclude='.DS_Store' --exclude='local.properties' \
    "$template/" "$stage/module/"

export KMP_NEW_PACKAGE="$package" KMP_MODULE_TYPE="$module_type"
perl -0pi -e '
    $plugin = s/id\("blazern\.lexisoup\.plugin\.feature"\)/id("blazern.lexisoup.plugin.$ENV{KMP_MODULE_TYPE}")/g;
    $namespace = s/namespace\s*=\s*"blazern\.lexisoup\.a\.template\.kmp\.common"/namespace = "$ENV{KMP_NEW_PACKAGE}"/g;
    die "Unexpected template plugin or namespace\n" unless $plugin == 1 && $namespace == 1;
' "$stage/module/build.gradle.kts"

# Only source files are rewritten; plugin IDs and binary resources stay intact.
while IFS= read -r -d '' source; do
    perl -0pi -e 's/\bblazern\.lexisoup\b/$ENV{KMP_NEW_PACKAGE}/g' "$source"
done < <(find "$stage/module/src" -type f \( -name '*.kt' -o -name '*.java' \) -print0)

package_path=${package//./\/}
while IFS= read -r -d '' source_root; do
    old_package="$source_root/blazern/lexisoup"
    [[ -d "$old_package" ]] || continue
    mv "$old_package" "$stage/package"
    mkdir -p "$source_root/$package_path"
    rsync -a "$stage/package/" "$source_root/$package_path/"
    rm -rf "$stage/package"
done < <(find "$stage/module/src" -type d \( -name kotlin -o -name java \) -print0)

cp -p "$settings" "$stage/settings.gradle.kts"
printf '\ninclude("%s")\n' "$module_path" >> "$stage/settings.gradle.kts"
ancestor=$parent
while [[ ! -d "$ancestor" ]]; do
    created_parents+=("$ancestor")
    ancestor=$(dirname -- "$ancestor")
done
mkdir -p "$parent"
[[ ! -e "$destination" && ! -L "$destination" ]] || die "Destination appeared during creation: $destination"
mv "$stage/module" "$destination"
installed=true
mv "$stage/settings.gradle.kts" "$settings"
committed=true
printf 'Created %s\nPackage: %s\nPlugin: blazern.lexisoup.plugin.%s\nRegistered in %s\n' \
    "$destination" "$package" "$module_type" "$settings"
