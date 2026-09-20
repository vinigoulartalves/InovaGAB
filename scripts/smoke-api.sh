#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
ARTIFACTS="${ARTIFACTS_DIR:-${ROOT_DIR}/artifacts/smoke}"

mkdir -p "$ARTIFACTS"

bash "${ROOT_DIR}/scripts/lib/wait-for-http.sh" "${BASE_URL}/health/live" 200 60 2
bash "${ROOT_DIR}/scripts/lib/wait-for-http.sh" "${BASE_URL}/health/ready" 200 60 2

curl -fsS "${BASE_URL}/health/live" -o "${ARTIFACTS}/health-live.json"
curl -fsS "${BASE_URL}/health/ready" -o "${ARTIFACTS}/health-ready.json"

if [[ ! -f "${ROOT_DIR}/.env" ]]; then
  echo "AVISO: .env ausente — pulando login smoke (execute scripts/setup-dev.sh)." >&2
  exit 0
fi

# shellcheck disable=SC1091
set -a
source "${ROOT_DIR}/.env"
set +a

if [[ -z "${DEV_PASSWORD_OPERADOR1:-}" ]]; then
  echo "AVISO: DEV_PASSWORD_OPERADOR1 ausente no .env — pulando login." >&2
  exit 0
fi

# Usuários seed da API (Development) — ver backend seed
LOGIN_BODY=$(printf '{"email":"operador1@inovagab.local","senha":"%s"}' "${DEV_PASSWORD_OPERADOR1}")
http_code=$(curl -sS -o "${ARTIFACTS}/login-operador1.json" -w "%{http_code}" \
  -X POST "${BASE_URL}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "${LOGIN_BODY}")

if [[ "$http_code" != "200" ]]; then
  echo "ERRO: login operador1 retornou HTTP $http_code" >&2
  cat "${ARTIFACTS}/login-operador1.json" >&2 || true
  exit 1
fi

echo "Smoke API OK (health + login operador1). Artefatos em ${ARTIFACTS}"
