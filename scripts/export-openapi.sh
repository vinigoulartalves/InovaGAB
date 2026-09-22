#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="${ROOT_DIR}/deliverables/openapi-runtime.json"
BASE="${BASE_URL:-http://127.0.0.1:8080}"
URL="${BASE%/}/swagger/v1/swagger.json"

mkdir -p "${ROOT_DIR}/deliverables"

if ! curl -fsS "${BASE%/}/health/ready" >/dev/null 2>&1; then
  echo "API não está pronta em ${BASE}. Execute: bash scripts/dev-up.sh" >&2
  exit 1
fi

curl -fsS "$URL" -o "$OUT"
echo "OpenAPI exportado: $OUT"

if command -v jq >/dev/null 2>&1; then
  echo "--- Paths (runtime) ---"
  jq -r '.paths | keys[]' "$OUT" | sort
fi
