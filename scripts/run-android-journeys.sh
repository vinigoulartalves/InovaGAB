#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Uso: $0 <git-commit>" >&2
  exit 2
fi

commit="$1"

set -a
# shellcheck disable=SC1091
source .env
set +a

mkdir -p artifacts/android-journey

package_ready=0
for _ in {1..90}; do
  if adb shell service check package 2>/dev/null | grep -q "found"; then
    package_ready=1
    break
  fi
  sleep 2
done

if [[ "$package_ready" -ne 1 ]]; then
  echo "Package Manager do emulador não ficou disponível em 180 segundos." >&2
  exit 1
fi

adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

set +e
adb shell am instrument -w -r \
  -e class com.fiap.inovagab.Sprint2JourneyInstrumentedTest \
  -e gitCommit "$commit" \
  -e apiBaseUrl "http://10.0.2.2:8080/" \
  -e operador1Email "operador1@inovagab.local" \
  -e operador1Password "$DEV_PASSWORD_OPERADOR1" \
  -e gestorEmail "gestor@inovagab.local" \
  -e gestorPassword "$DEV_PASSWORD_GESTOR" \
  -e liderEmail "lider@inovagab.local" \
  -e liderPassword "$DEV_PASSWORD_LIDER" \
  com.fiap.inovagab.test/androidx.test.runner.AndroidJUnitRunner \
  | tee artifacts/android-journey/instrumentation.txt
instrument_exit=${PIPESTATUS[0]}
set -e

if grep -Eq "FAILURES!!!|INSTRUMENTATION_FAILED|INSTRUMENTATION_CODE: -1" artifacts/android-journey/instrumentation.txt; then
  instrument_exit=1
fi

mkdir -p artifacts/android-journey/screenshots
adb exec-out run-as com.fiap.inovagab \
  tar -C files -cf - "sprint2-evidence/$commit" \
  | tar -C artifacts/android-journey/screenshots -xf - || true
python3 scripts/lib/write-evidence-index.py \
  --android-dir artifacts/android-journey \
  --out artifacts/EVIDENCE_INDEX.md \
  --instrument-exit "$instrument_exit" || true

exit "$instrument_exit"
