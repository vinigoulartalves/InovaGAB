$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
& bash "$Root/scripts/package-delivery.sh"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
