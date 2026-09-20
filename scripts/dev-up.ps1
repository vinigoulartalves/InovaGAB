$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
& "$Root\scripts\setup-dev.ps1"
docker compose up -d --build mongo mongo-init api
$base = if ($env:BASE_URL) { $env:BASE_URL } else { "http://127.0.0.1:8080" }
bash "$Root/scripts/lib/wait-for-http.sh" "$base/health/ready" 200 180 3
Write-Host "API pronta em $base"
