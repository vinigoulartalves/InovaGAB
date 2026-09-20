$ErrorActionPreference = "Stop"
$RootDir = Split-Path -Parent $PSScriptRoot
$EnvFile = Join-Path $RootDir ".env"

if (Test-Path $EnvFile) {
    Write-Host ".env já existe em $EnvFile — não sobrescrevendo."
    exit 0
}

function New-RandomBase64([int]$Bytes = 48) {
    $buffer = New-Object byte[] $Bytes
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($buffer)
    return [Convert]::ToBase64String($buffer)
}

function New-DevPassword() {
    return (New-RandomBase64 18).Replace("=", "").Replace("+", "").Substring(0, 16)
}

$jwt = New-RandomBase64 48
$content = @"
# Gerado por scripts/setup-dev.ps1 em $(Get-Date).ToUniversalTime().ToString("o")
JWT_SECRET=$jwt
SEED_ENABLED=true
AI_ENABLED=false
AI_API_KEY=
AI_MODEL=gemini-2.0-flash
DEV_PASSWORD_OPERADOR1=$(New-DevPassword)
DEV_PASSWORD_OPERADOR2=$(New-DevPassword)
DEV_PASSWORD_GESTOR=$(New-DevPassword)
DEV_PASSWORD_LIDER=$(New-DevPassword)
"@

Set-Content -Path $EnvFile -Value $content -NoNewline
Write-Host "Arquivo .env criado em $EnvFile"
