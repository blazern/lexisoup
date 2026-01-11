# How to initialize locally

## MacOS

**Please ensure no command is run with sudo to avoid using the system Ruby**.

1. Install rbenv: https://github.com/rbenv/rbenv
2. Install `rbenv`: `rbenv install 3.4.8` (no need to run `rbenv local 3.4.8` because `.ruby-version` already exists).
3. Ensure system Ruby is not used by running `which ruby` and `which gem`.
4. Install bundler: `gem install bundler:2.7.2` (source: https://docs.fastlane.tools/getting-started/android/setup/).
5. Fetch `fastlane` (and other dependencies) using `bundle _2.7.2_ install`.
