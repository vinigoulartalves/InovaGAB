#!/usr/bin/env bash
# Aguarda URL HTTP retornar código esperado (sem sleep cego após sucesso).
set -euo pipefail

URL="${1:?URL obrigatória}"
EXPECTED="${2:-200}"
TIMEOUT_SEC="${3:-120}"
INTERVAL_SEC="${4:-2}"

deadline=$(( $(date +%s) + TIMEOUT_SEC ))
last_code=""

while [ "$(date +%s)" -lt "$deadline" ]; do
  if code=$(curl -sS -o /dev/null -w "%{http_code}" "$URL" 2>/dev/null || true); then
    last_code="$code"
    if [ "$code" = "$EXPECTED" ]; then
      echo "OK: $URL → HTTP $code"
      exit 0
    fi
  fi
  sleep "$INTERVAL_SEC"
done

echo "ERRO: timeout ${TIMEOUT_SEC}s aguardando $URL (último HTTP: ${last_code:-n/a}, esperado: $EXPECTED)" >&2
exit 1
