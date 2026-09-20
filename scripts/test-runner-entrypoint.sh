#!/usr/bin/env bash
set -euo pipefail

cd /src
ARTIFACTS="${ARTIFACTS_DIR:-/src/artifacts}"
RESULTS="${ARTIFACTS}/test-results"
COVERAGE="${ARTIFACTS}/coverage"
LOG="${ARTIFACTS}/test-runner.log"

mkdir -p "$RESULTS" "$COVERAGE"
exec > >(tee -a "$LOG") 2>&1

echo "==> InovaGAB test-runner ($(date -u +%Y-%m-%dT%H:%M:%SZ))"
echo "MONGODB_URI=${MONGODB_URI:-not set}"

dotnet restore backend/InovaGAB.sln

run_test_project() {
  local csproj="$1"
  local name="$2"
  local extra_filter="${3:-}"

  local proj_results="${RESULTS}/${name}"
  local proj_coverage="${COVERAGE}/${name}"
  mkdir -p "$proj_results" "$proj_coverage"

  local filter="FullyQualifiedName!~InovaGAB.IaExternalTests"
  if [[ -n "$extra_filter" ]]; then
    filter="${filter}&${extra_filter}"
  fi

  echo ""
  echo "==> dotnet test: ${name} (${csproj})"
  dotnet test "$csproj" -c Release --no-restore \
    --verbosity normal \
    --filter "$filter" \
    --results-directory "$proj_results" \
    --logger "trx;LogFileName=${name}.trx" \
    /p:CollectCoverage=true \
    /p:CoverletOutput="${proj_coverage}/" \
    /p:CoverletOutputFormat=cobertura \
    /p:CoverletOutputName="${name}"
}

run_test_project "tests/InovaGAB.UnitTests/InovaGAB.UnitTests.csproj" "UnitTests"
run_test_project "tests/InovaGAB.IntegrationTests/InovaGAB.IntegrationTests.csproj" "IntegrationTests"

if [[ "${RUN_IA_EXTERNAL_TESTS:-}" == "true" || "${RUN_IA_EXTERNAL_TESTS:-}" == "yes" ]]; then
  if [[ -z "${AI_API_KEY:-}" ]]; then
    echo "RUN_IA_EXTERNAL_TESTS definido mas AI_API_KEY ausente — pulando IaExternalTests"
  else
    echo "==> IaExternalTests (opt-in, chave presente)"
    run_test_project "tests/InovaGAB.IaExternalTests/InovaGAB.IaExternalTests.csproj" "IaExternalTests" ""
  fi
else
  echo "==> IaExternalTests ignorado (defina RUN_IA_EXTERNAL_TESTS=true + AI_API_KEY para opt-in)"
fi

echo ""
echo "==> Resumo TRX"
python3 /src/scripts/lib/parse-trx-results.py "$RESULTS" "${ARTIFACTS}/test-summary.json"
cat "${ARTIFACTS}/test-summary.txt" 2>/dev/null || true

if ! python3 /src/scripts/lib/parse-trx-results.py --check "$RESULTS"; then
  echo "Falha: testes com erro ou contagem inválida." >&2
  exit 1
fi

echo "Testes backend concluídos com sucesso. TRX em ${RESULTS}, cobertura em ${COVERAGE}"
