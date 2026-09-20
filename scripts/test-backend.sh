#!/usr/bin/env bash
# Testes de integração + unitários backend com Mongo replica set real (compose profile tests).
# Não monta Docker socket no test-runner. Não reseta banco de desenvolvimento inovagab.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

ARTIFACTS="${ARTIFACTS_DIR:-${ROOT_DIR}/artifacts}"
RESULTS="${ARTIFACTS}/test-results"
COVERAGE="${ARTIFACTS}/coverage"

mkdir -p "$RESULTS" "$COVERAGE"

echo "Garantindo Mongo rs0 (perfil tests)..."
docker compose --profile tests up -d mongo mongo-init

bash "${ROOT_DIR}/scripts/lib/wait-for-http.sh" \
  "http://127.0.0.1:8080/health/ready" 200 5 1 2>/dev/null || true

export ARTIFACTS_DIR="$ARTIFACTS"

docker compose --profile tests run --rm test-runner

echo "Resultados: ${RESULTS}"
