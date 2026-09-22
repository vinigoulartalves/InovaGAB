$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Out = Join-Path $Root "deliverables\openapi-runtime.json"
New-Item -ItemType Directory -Force -Path (Join-Path $Root "deliverables") | Out-Null
$Base = if ($env:BASE_URL) { $env:BASE_URL } else { "http://127.0.0.1:8080" }
$Base = $Base.TrimEnd("/")
$Url = "$Base/swagger/v1/swagger.json"

try {
  Invoke-WebRequest -Uri "$Base/health/ready" -UseBasicParsing | Out-Null
} catch {
  Write-Error "API não está pronta em $Base. Execute: .\scripts\dev-up.ps1"
}

Invoke-WebRequest -Uri $Url -UseBasicParsing -OutFile $Out
Write-Host "OpenAPI exportado: $Out"
