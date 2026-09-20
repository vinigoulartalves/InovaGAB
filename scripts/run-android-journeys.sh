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

set +e
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.fiap.inovagab.Sprint2JourneyInstrumentedTest \
  -Pandroid.testInstrumentationRunnerArguments.gitCommit="$commit" \
  -Pandroid.testInstrumentationRunnerArguments.apiBaseUrl="http://10.0.2.2:8080/" \
  -Pandroid.testInstrumentationRunnerArguments.operador1Email="operador1@inovagab.local" \
  -Pandroid.testInstrumentationRunnerArguments.operador1Password="$DEV_PASSWORD_OPERADOR1" \
  -Pandroid.testInstrumentationRunnerArguments.gestorEmail="gestor@inovagab.local" \
  -Pandroid.testInstrumentationRunnerArguments.gestorPassword="$DEV_PASSWORD_GESTOR" \
  -Pandroid.testInstrumentationRunnerArguments.liderEmail="lider@inovagab.local" \
  -Pandroid.testInstrumentationRunnerArguments.liderPassword="$DEV_PASSWORD_LIDER" \
  --no-daemon
instrument_exit=$?
set -e

remote_dir="/sdcard/Android/data/com.fiap.inovagab/files/sprint2-evidence/$commit"
adb pull "$remote_dir" artifacts/android-journey/screenshots || true
python3 scripts/lib/write-evidence-index.py \
  --android-dir artifacts/android-journey \
  --out artifacts/EVIDENCE_INDEX.md \
  --instrument-exit "$instrument_exit" || true

exit "$instrument_exit"
