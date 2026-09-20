#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="${ROOT_DIR}/docs/sprint2/openapi-runtime.json"
BASE="${BASE_URL:-http://127.0.0.1:8080}"
URL="${BASE%/}/swagger/v1/swagger.json"

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

CONTRACT="${ROOT_DIR}/docs/sprint2/openapi.yaml"
if command -v python3 >/dev/null 2>&1; then
  python3 - <<'PY' "$CONTRACT" "$OUT"
import sys, re, json
yaml_path, json_path = sys.argv[1], sys.argv[2]
with open(yaml_path) as f:
    yaml_paths = set(re.findall(r'^  (/[^\s:]+):', f.read(), re.M))
with open(json_path) as f:
    runtime_paths = set(json.load(f).get("paths", {}).keys())
only_yaml = sorted(yaml_paths - runtime_paths)
only_rt = sorted(runtime_paths - yaml_paths)
print("Somente openapi.yaml:", only_yaml or "(nenhum)")
print("Somente runtime:", only_rt or "(nenhum)")
PY
fi
