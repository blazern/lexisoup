#!/usr/bin/env python3
from __future__ import annotations

import argparse
import glob
import shutil
import subprocess
from pathlib import Path


def run(cmd: list[str], *, cwd: Path | None = None) -> None:
    """Pretty wrapper around subprocess.check_call."""
    print(f"> {' '.join(cmd)}")
    subprocess.check_call(cmd, cwd=cwd)


def newest_file(pattern: str) -> Path | None:
    matches = [Path(p) for p in glob.glob(pattern)]
    if not matches:
        return None
    return max(matches, key=lambda p: p.stat().st_mtime)


def main() -> None:
    parser = argparse.ArgumentParser(
        description=(
            "Build Android Release APK + AAB"
        ),
    )
    parser.add_argument(
        "--project-dir",
        type=Path,
        required=True,
        help="KMP project root directory",
    )
    parser.add_argument(
        "--version-name",
        required=True,
        help="Gradle property versionName",
    )
    parser.add_argument(
        "--version-code",
        required=True,
        help="Gradle property versionCode",
    )
    parser.add_argument(
        "--keystore-path",
        type=Path,
        required=True,
        help="Path to the keystore file (e.g., /path/to/keystore.jks)",
    )
    parser.add_argument(
        "--keystore-password",
        required=True,
        help="Keystore password",
    )
    parser.add_argument(
        "--keystore-key-alias",
        required=True,
        help="Keystore key alias",
    )
    parser.add_argument(
        "--keystore-key-password",
        required=True,
        help="Keystore key password",
    )
    parser.add_argument(
        "--out-aab-path",
        type=Path,
        required=True,
        help="Where to copy the aab artifact",
    )
    parser.add_argument(
        "--out-apk-path",
        type=Path,
        required=True,
        help="Where to copy the apk artifact",
    )

    args = parser.parse_args()

    project_dir: Path = args.project_dir.resolve()
    gradlew: Path = (project_dir / "gradlew").resolve()
    module: str = "androidApp"
    module_dir = project_dir / module
    version_name: str = args.version_name
    version_code: str = str(args.version_code)

    keystore_path: Path = args.keystore_path.resolve()
    keystore_password: str = args.keystore_password
    keystore_key_alias: str = args.keystore_key_alias
    keystore_key_password: str = args.keystore_key_password

    out_aab_path: Path = args.out_aab_path.resolve()
    out_apk_path: Path = args.out_apk_path.resolve()

    if not project_dir.is_dir():
        raise SystemExit(f"{project_dir} is not a directory")

    if not gradlew.is_file():
        raise SystemExit(f"{gradlew} is not a file")

    if not keystore_path.is_file():
        raise SystemExit(f"{keystore_path} is not a file")

    gradle_props = [
        f"-PversionName={version_name}",
        f"-PversionCode={version_code}",
        f"-PkeystoreFile={str(keystore_path)}",
        f"-PkeystorePassword={keystore_password}",
        f"-PkeystoreKeyAlias={keystore_key_alias}",
        f"-PkeystoreKeyPassword={keystore_key_password}",
    ]

    print("> Building Android release artifacts")
    run(
        [str(gradlew), f":{module}:assembleRelease", *gradle_props],
        cwd=project_dir,
    )
    run(
        [str(gradlew), f":{module}:bundleRelease", *gradle_props],
        cwd=project_dir,
    )

    apk_pattern = str(module_dir / "build" / "outputs" / "apk" / "release" / "*.apk")
    aab_pattern = str(module_dir / "build" / "outputs" / "bundle" / "release" / "*.aab")

    apk = newest_file(apk_pattern)
    aab = newest_file(aab_pattern)

    if apk is None:
        raise SystemExit(f"apk not found. Expected something like: {apk_pattern}")

    if aab is None:
        raise SystemExit(f"aab not found. Expected something like: {aab_pattern}")

    print("> Copying artifacts")
    out_aab_path.parent.mkdir(parents=True, exist_ok=True)
    out_apk_path.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(apk, out_apk_path)
    shutil.copy2(aab, out_aab_path)

    print("✔ Build finished")
    print(f"AAB: {out_aab_path}")
    print(f"APK: {out_apk_path}")


if __name__ == "__main__":
    main()
