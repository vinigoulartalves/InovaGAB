#!/usr/bin/env bash
set -euo pipefail

cd /src
ARTIFACTS="${ARTIFACTS_DIR:-/src/artifacts}"
RESULTS="${ARTIFACTS}/test-results"
COVERAGE="${ARTIFACTS}/coverage"

mkdir -p "$RESULTS" "$COVERAGE"

dotnet restore backend/InovaGAB.sln

dotnet test backend/InovaGAB.sln -c Release --no-restore \
  --verbosity normal \
  --filter "FullyQualifiedName!~InovaGAB.IaExternalTests" \
  --results-directory "$RESULTS" \
  --logger "trx;LogFileName=backend.trx" \
  /p:CollectCoverage=true \
  /p:CoverletOutput="${COVERAGE}/" \
  /p:CoverletOutputFormat=cobertura,opencover

echo "Testes concluídos. TRX em ${RESULTS}"
