#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env"

if [[ -f "${ENV_FILE}" ]]; then
  echo ".env já existe em ${ENV_FILE} — não sobrescrevendo."
  exit 0
fi

gen_secret() {
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -base64 48 | tr -d '\n'
  else
    head -c 48 /dev/urandom | base64 | tr -d '\n'
  fi
}

gen_password() {
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -base64 18 | tr -d '\n/+=' | head -c 16
  else
    head -c 16 /dev/urandom | base64 | tr -d '\n/+=' | head -c 16
  fi
}

JWT_SECRET="$(gen_secret)"
PASS_OP1="$(gen_password)"
PASS_OP2="$(gen_password)"
PASS_GESTOR="$(gen_password)"
PASS_LIDER="$(gen_password)"

cat > "${ENV_FILE}" <<EOF
# Gerado por scripts/setup-dev.sh em $(date -u +"%Y-%m-%dT%H:%M:%SZ")
JWT_SECRET=${JWT_SECRET}
SEED_ENABLED=true
AI_ENABLED=false
AI_API_KEY=
AI_MODEL=gemini-2.0-flash
DEV_PASSWORD_OPERADOR1=${PASS_OP1}
DEV_PASSWORD_OPERADOR2=${PASS_OP2}
DEV_PASSWORD_GESTOR=${PASS_GESTOR}
DEV_PASSWORD_LIDER=${PASS_LIDER}
EOF

chmod 600 "${ENV_FILE}"
echo "Arquivo .env criado em ${ENV_FILE}"
