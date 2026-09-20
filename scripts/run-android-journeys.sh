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

mkdir -p artifacts/android-journey/screenshots
: > artifacts/android-journey/instrumentation.txt

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

test_methods=(
  "A01_login_invalido_mostra_erro"
  "A02_operador_home_orientacoes_ideias_ranking"
  "A03_gestor_gestao_ideias_ia_indisponivel_ou_ok"
  "A04_lider_dashboard_filtros_graficos"
)

instrument_exit=0
for test_method in "${test_methods[@]}"; do
  case_log="artifacts/android-journey/${test_method}.txt"
  echo "===== ${test_method} =====" | tee -a artifacts/android-journey/instrumentation.txt

  # O processo de instrumentação ainda não está ativo, portanto a limpeza é segura.
  adb shell pm clear com.fiap.inovagab >/dev/null
  adb shell pm clear com.fiap.inovagab.test >/dev/null
  adb reverse tcp:8080 tcp:8080

  set +e
  adb shell am instrument -w -r \
    -e class "com.fiap.inovagab.Sprint2JourneyInstrumentedTest#${test_method}" \
    -e gitCommit "$commit" \
    -e apiBaseUrl "http://127.0.0.1:8080/" \
    -e operador1Email "operador1@inovagab.local" \
    -e operador1Password "$DEV_PASSWORD_OPERADOR1" \
    -e gestorEmail "gestor@inovagab.local" \
    -e gestorPassword "$DEV_PASSWORD_GESTOR" \
    -e liderEmail "lider@inovagab.local" \
    -e liderPassword "$DEV_PASSWORD_LIDER" \
    com.fiap.inovagab.test/androidx.test.runner.AndroidJUnitRunner \
    | tee "$case_log"
  case_exit=${PIPESTATUS[0]}
  set -e

  cat "$case_log" >> artifacts/android-journey/instrumentation.txt
  if grep -Eq "FAILURES!!!|INSTRUMENTATION_FAILED|AssumptionViolatedException" "$case_log"; then
    case_exit=1
  fi
  if [[ "$case_exit" -ne 0 ]]; then
    instrument_exit=1
  fi

  evidence_tar="artifacts/android-journey/${test_method}.tar"
  if adb exec-out run-as com.fiap.inovagab \
    tar -C files -cf - "sprint2-evidence/$commit" > "$evidence_tar" 2>/dev/null; then
    if tar -tf "$evidence_tar" >/dev/null 2>&1; then
      tar -C artifacts/android-journey/screenshots -xf "$evidence_tar" || true
    fi
  fi
  rm -f "$evidence_tar"
done

python3 scripts/lib/write-evidence-index.py \
  --android-dir artifacts/android-journey \
  --out artifacts/EVIDENCE_INDEX.md \
  --instrument-exit "$instrument_exit" || true

exit "$instrument_exit"
