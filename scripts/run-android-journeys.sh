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

adb reverse tcp:8080 tcp:8080
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

set +e
adb shell am instrument -w -r \
  -e class com.fiap.inovagab.Sprint2JourneyInstrumentedTest \
  -e gitCommit "$commit" \
  -e apiBaseUrl "http://127.0.0.1:8080/" \
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

if grep -Eq "FAILURES!!!|INSTRUMENTATION_FAILED|AssumptionViolatedException" artifacts/android-journey/instrumentation.txt; then
  instrument_exit=1
fi

mkdir -p artifacts/android-journey/screenshots
evidence_tar="artifacts/android-journey/evidence.tar"
if adb exec-out run-as com.fiap.inovagab \
  tar -C files -cf - "sprint2-evidence/$commit" > "$evidence_tar"; then
  tar -C artifacts/android-journey/screenshots -xf "$evidence_tar" || true
fi
rm -f "$evidence_tar"
python3 scripts/lib/write-evidence-index.py \
  --android-dir artifacts/android-journey \
  --out artifacts/EVIDENCE_INDEX.md \
  --instrument-exit "$instrument_exit" || true

exit "$instrument_exit"
