#!/usr/bin/env bash
# Alias legado — preferir scripts/smoke-api.sh
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
exec bash "${ROOT_DIR}/scripts/smoke-api.sh"
