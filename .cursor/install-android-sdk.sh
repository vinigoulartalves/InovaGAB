#!/usr/bin/env bash
# Idempotent installer for the Android SDK pieces required to build this project.
# Runs from the repo root on every Cloud Agent boot via .cursor/environment.json.
#
# It performs the following steps (skipping any already done):
#   1. Ensures ANDROID_HOME / PATH are exported in ~/.bashrc so every
#      subsequent shell (and ./gradlew) can find the SDK.
#   2. Downloads the Android command-line tools into $ANDROID_HOME.
#   3. Installs platform-tools, platforms;android-34 and build-tools;34.0.0.
#   4. Auto-accepts every SDK license.
#
# Total cold install ~250 MB; warm runs are essentially a no-op.

set -euo pipefail

ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
ANDROID_HOME="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
CMDLINE_TOOLS_VERSION="11076708"
CMDLINE_TOOLS_ZIP="commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/${CMDLINE_TOOLS_ZIP}"

PLATFORM_VERSION="android-34"
BUILD_TOOLS_VERSION="34.0.0"

log() { printf '\033[1;34m[android-sdk]\033[0m %s\n' "$*"; }

ensure_packages() {
    local missing=()
    for bin in curl unzip; do
        command -v "$bin" >/dev/null 2>&1 || missing+=("$bin")
    done
    if [ "${#missing[@]}" -gt 0 ]; then
        log "Installing missing prerequisites: ${missing[*]}"
        sudo apt-get update -y
        sudo DEBIAN_FRONTEND=noninteractive apt-get install -y "${missing[@]}"
    fi
}

persist_env() {
    local rc="$HOME/.bashrc"
    local marker="# >>> android-sdk env (managed by .cursor/install-android-sdk.sh) >>>"
    local end_marker="# <<< android-sdk env <<<"

    if ! grep -qF "$marker" "$rc" 2>/dev/null; then
        log "Persisting ANDROID_HOME / PATH in $rc"
        {
            printf '\n%s\n' "$marker"
            printf 'export ANDROID_SDK_ROOT="%s"\n' "$ANDROID_SDK_ROOT"
            printf 'export ANDROID_HOME="%s"\n' "$ANDROID_HOME"
            printf 'export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"\n'
            printf '%s\n' "$end_marker"
        } >> "$rc"
    fi
}

install_cmdline_tools() {
    local target="$ANDROID_HOME/cmdline-tools/latest"
    if [ -x "$target/bin/sdkmanager" ]; then
        log "cmdline-tools already present at $target"
        return 0
    fi

    log "Downloading Android command-line tools ${CMDLINE_TOOLS_VERSION}"
    mkdir -p "$ANDROID_HOME/cmdline-tools"
    local tmpdir
    tmpdir="$(mktemp -d)"
    trap 'rm -rf "$tmpdir"' RETURN
    curl -fsSL --retry 5 --retry-delay 4 -o "$tmpdir/${CMDLINE_TOOLS_ZIP}" "$CMDLINE_TOOLS_URL"
    unzip -q "$tmpdir/${CMDLINE_TOOLS_ZIP}" -d "$tmpdir"
    rm -rf "$target"
    mv "$tmpdir/cmdline-tools" "$target"
}

accept_licenses() {
    local sdkmanager="$1"
    # `yes` is killed with SIGPIPE once sdkmanager has consumed all the prompts
    # it needs, which would otherwise trip pipefail. Run the pipeline in a
    # subshell with pipefail disabled so we only care about sdkmanager's exit.
    ( set +o pipefail; yes | "$sdkmanager" --sdk_root="$ANDROID_HOME" --licenses >/dev/null )
}

accept_licenses_and_install_packages() {
    local sdkmanager="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"

    if [ ! -x "$sdkmanager" ]; then
        echo "sdkmanager not found at $sdkmanager" >&2
        exit 1
    fi

    log "Accepting SDK licenses"
    accept_licenses "$sdkmanager"

    log "Installing platform-tools, platforms;${PLATFORM_VERSION}, build-tools;${BUILD_TOOLS_VERSION}"
    "$sdkmanager" --sdk_root="$ANDROID_HOME" \
        "platform-tools" \
        "platforms;${PLATFORM_VERSION}" \
        "build-tools;${BUILD_TOOLS_VERSION}" >/dev/null

    accept_licenses "$sdkmanager"
}

main() {
    ensure_packages
    mkdir -p "$ANDROID_HOME"
    persist_env
    install_cmdline_tools
    accept_licenses_and_install_packages

    export ANDROID_SDK_ROOT ANDROID_HOME
    export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

    log "Installed components:"
    "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$ANDROID_HOME" --list_installed || true
    log "Done. ANDROID_HOME=$ANDROID_HOME"
}

main "$@"
