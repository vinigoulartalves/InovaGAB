$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
docker compose --profile tests up -d mongo mongo-init
$env:ARTIFACTS_DIR = Join-Path $Root "artifacts"
docker compose --profile tests run --rm test-runner
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
