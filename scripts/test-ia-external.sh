#!/usr/bin/env bash
# Suíte opt-in IA real (Gemini). Sem AI_API_KEY: falha explícita (não verde falso).
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ -z "${AI_API_KEY:-}" && -z "${AI__ApiKey:-}" && -z "${GEMINI_API_KEY:-}" ]]; then
  echo "PENDENTE: defina AI_API_KEY para executar testes IA reais (não executado)." >&2
  exit 2
fi

export MONGODB_URI="${MONGODB_URI:-mongodb://127.0.0.1:27017/?replicaSet=rs0&directConnection=true}"

ARTIFACTS="${ARTIFACTS_DIR:-${ROOT_DIR}/artifacts}/test-results"
mkdir -p "$ARTIFACTS"

docker compose --profile tests up -d mongo mongo-init

dotnet test tests/InovaGAB.IaExternalTests/InovaGAB.IaExternalTests.csproj -c Release \
  --results-directory "$ARTIFACTS" \
  --logger "trx;LogFileName=ia-external.trx"
