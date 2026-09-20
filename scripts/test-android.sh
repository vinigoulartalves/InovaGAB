#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

ARTIFACTS="${ARTIFACTS_DIR:-${ROOT_DIR}/artifacts}"
mkdir -p "${ARTIFACTS}/android"

./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug \
  --no-daemon

cp -f app/build/outputs/apk/debug/app-debug.apk "${ARTIFACTS}/android/" 2>/dev/null || true
cp -rf app/build/reports/tests/testDebugUnitTest "${ARTIFACTS}/android/unit-test-report" 2>/dev/null || true

echo "APK e relatórios em ${ARTIFACTS}/android"
