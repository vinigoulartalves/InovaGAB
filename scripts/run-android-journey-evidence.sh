#!/usr/bin/env bash
# Jornadas instrumentadas + capturas PNG. Requer emulador/dispositivo, API em 10.0.2.2:8080 e .env com senhas seed.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

ARTIFACTS="${ARTIFACTS_DIR:-${ROOT_DIR}/artifacts}"
ANDROID_EVIDENCE="${ARTIFACTS}/android-journey"
mkdir -p "$ANDROID_EVIDENCE"

if [[ ! -f "${ROOT_DIR}/.env" ]]; then
  echo "Crie .env com scripts/setup-dev.sh antes de rodar jornadas Android." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1091
source "${ROOT_DIR}/.env"
set +a

COMMIT="$(git rev-parse HEAD 2>/dev/null || echo unknown)"

echo "==> Subindo API (Compose) se necessário"
if ! curl -fsS "http://127.0.0.1:8080/health/ready" >/dev/null 2>&1; then
  bash "${ROOT_DIR}/scripts/dev-up.sh"
fi

if ! command -v adb >/dev/null 2>&1; then
  echo "adb não encontrado" >&2
  exit 1
fi

adb wait-for-device
SERIAL="$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')"
if [[ -z "$SERIAL" ]]; then
  echo "Nenhum dispositivo/emulador Android conectado." >&2
  exit 1
fi

./gradlew :app:assembleDebug :app:assembleDebugAndroidTest --no-daemon
adb -s "$SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "$SERIAL" install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

PKG="com.fiap.inovagab"
RUNNER="${PKG}.test/androidx.test.runner.AndroidJUnitRunner"

ARGS=(
  -e gitCommit "$COMMIT"
  -e apiBaseUrl "http://10.0.2.2:8080/"
  -e operador1Email "operador1@inovagab.local"
  -e operador1Password "${DEV_PASSWORD_OPERADOR1}"
  -e gestorEmail "gestor@inovagab.local"
  -e gestorPassword "${DEV_PASSWORD_GESTOR}"
  -e liderEmail "lider@inovagab.local"
  -e liderPassword "${DEV_PASSWORD_LIDER}"
)

echo "==> Instrumentação (credenciais só via args adb, não gravadas no repo)"
set +e
adb -s "$SERIAL" shell am instrument -w -r "${ARGS[@]}" "$RUNNER" \
  -e class com.fiap.inovagab.Sprint2JourneyInstrumentedTest \
  2>&1 | tee "${ANDROID_EVIDENCE}/instrument.log"
INST_EXIT=${PIPESTATUS[0]}
set -e

REMOTE_DIR="/sdcard/Android/data/${PKG}/files/sprint2-evidence/${COMMIT}"
adb -s "$SERIAL" pull "$REMOTE_DIR" "${ANDROID_EVIDENCE}/screenshots" 2>/dev/null || true

python3 "${ROOT_DIR}/scripts/lib/write-evidence-index.py" \
  --backend-summary "${ARTIFACTS}/test-summary.json" \
  --android-dir "${ANDROID_EVIDENCE}" \
  --out "${ARTIFACTS}/EVIDENCE_INDEX.md" \
  --instrument-exit "$INST_EXIT"

if [[ "$INST_EXIT" -ne 0 ]]; then
  echo "Instrumentação falhou (exit $INST_EXIT). Ver ${ANDROID_EVIDENCE}/instrument.log" >&2
  exit "$INST_EXIT"
fi

echo "Evidências Android em ${ANDROID_EVIDENCE}"
