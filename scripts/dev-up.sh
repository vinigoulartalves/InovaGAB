#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

bash "${ROOT_DIR}/scripts/setup-dev.sh"

echo "Subindo Mongo (rs0) + API (perfil padrão, banco inovagab de desenvolvimento)..."
docker compose up -d --build mongo mongo-init api

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
bash "${ROOT_DIR}/scripts/lib/wait-for-http.sh" "${BASE_URL}/health/ready" 200 180 3

echo "API pronta em ${BASE_URL}"
echo "Banco de desenvolvimento: inovagab (via compose). Testes usam bancos isolados (inovagab_test_*)."
