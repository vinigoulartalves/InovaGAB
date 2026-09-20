#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

STAMP="$(date -u +"%Y%m%dT%H%M%SZ")"
COMMIT="$(git rev-parse --short HEAD 2>/dev/null || echo unknown)"
ARTIFACTS="${ARTIFACTS_DIR:-${ROOT_DIR}/artifacts}"
MANIFEST="${ARTIFACTS}/evidence-${STAMP}.txt"

mkdir -p "$ARTIFACTS"

{
  echo "timestamp_utc=${STAMP}"
  echo "git_commit=${COMMIT}"
  echo "dotnet=$(dotnet --version 2>/dev/null || echo n/a)"
  echo "java=$(java -version 2>&1 | head -1 || echo n/a)"
  echo "docker=$(docker --version 2>/dev/null || echo n/a)"
  echo "compose=$(docker compose version 2>/dev/null || echo n/a)"
  if [[ -d "${ARTIFACTS}/test-results" ]]; then
    echo "trx_files=$(find "${ARTIFACTS}/test-results" -name '*.trx' 2>/dev/null | wc -l)"
    if [[ -f "${ARTIFACTS}/test-summary.txt" ]]; then
      echo "backend_summary=$(cat "${ARTIFACTS}/test-summary.txt")"
    fi
  fi
} > "$MANIFEST"

if [[ -f "${ARTIFACTS}/test-summary.json" ]] || [[ -d "${ARTIFACTS}/android-journey" ]]; then
  python3 "${ROOT_DIR}/scripts/lib/write-evidence-index.py" \
    --backend-summary "${ARTIFACTS}/test-summary.json" \
    --android-dir "${ARTIFACTS}/android-journey" \
    --out "${ARTIFACTS}/EVIDENCE_INDEX.md" \
    --instrument-exit 0 2>/dev/null || true
fi

echo "Evidência registrada em ${MANIFEST}"
