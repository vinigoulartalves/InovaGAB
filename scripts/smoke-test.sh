#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"

echo "Smoke: GET ${BASE_URL}/health/live"
curl -fsS "${BASE_URL}/health/live" | tee /tmp/inovagab-live.json

echo "Smoke: GET ${BASE_URL}/health/ready"
curl -fsS "${BASE_URL}/health/ready" | tee /tmp/inovagab-ready.json

echo "Smoke OK"
