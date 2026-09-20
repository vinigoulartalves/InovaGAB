#!/usr/bin/env bash
# Executa testes no host: exige MONGODB_URI apontando para replica set (ex.: após compose up mongo).
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

ARTIFACTS="${ARTIFACTS_DIR:-${ROOT_DIR}/artifacts}"
RESULTS="${ARTIFACTS}/test-results"
mkdir -p "$RESULTS"

export MONGODB_URI="${MONGODB_URI:-mongodb://127.0.0.1:27017/?replicaSet=rs0&directConnection=true}"
export Mongo__DatabaseName="${Mongo__DatabaseName:-inovagab_test_host}"

if ! command -v dotnet >/dev/null 2>&1; then
  echo "ERRO: dotnet SDK não encontrado. Use global.json (8.0.425)." >&2
  exit 1
fi

dotnet restore backend/InovaGAB.sln

dotnet test backend/InovaGAB.sln -c Release --no-restore \
  --verbosity normal \
  --filter "FullyQualifiedName!~InovaGAB.IaExternalTests" \
  --results-directory "$RESULTS" \
  --logger "trx;LogFileName=backend-integration.trx"

echo "TRX em ${RESULTS}"
