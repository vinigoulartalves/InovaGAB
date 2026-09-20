#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

DELIV="${ROOT_DIR}/deliverables"
mkdir -p "$DELIV"

BACKEND_ZIP="${DELIV}/InovaGAB_Backend_Sprint2.zip"
ANDROID_ZIP="${DELIV}/InovaGAB_Android_Sprint2.zip"
MANIFEST="${DELIV}/MANIFEST_ENTREGA.md"

COMMIT="$(git rev-parse HEAD)"
COMMIT_SHORT="$(git rev-parse --short HEAD)"
DOTNET_VER="$(dotnet --version 2>/dev/null || echo unknown)"
DATE_UTC="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"

echo "==> Build APK debug (baseURL emulador 10.0.2.2:8080)"
bash "${ROOT_DIR}/scripts/test-android.sh"

APK_SRC="${ROOT_DIR}/app/build/outputs/apk/debug/app-debug.apk"
if [[ ! -f "$APK_SRC" ]]; then
  echo "APK não encontrado: $APK_SRC" >&2
  exit 1
fi

sha256_file() {
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$1" | awk '{print $1}'
  else
    shasum -a 256 "$1" | awk '{print $1}'
  fi
}

STAGING="$(mktemp -d)"
trap 'rm -rf "$STAGING"' EXIT

tar_copy() {
  local src="$1" dst="$2"
  shift 2
  mkdir -p "$dst"
  ( cd "$src" && tar cf - "$@" . ) | ( cd "$dst" && tar xf - )
}

# --- Backend package ---
BE_DIR="${STAGING}/InovaGAB_Backend_Sprint2"
mkdir -p "$BE_DIR"

tar_copy "${ROOT_DIR}/backend" "${BE_DIR}/backend" \
  --exclude='bin' --exclude='obj' --exclude='.env'

tar_copy "${ROOT_DIR}/infra" "${BE_DIR}/infra"
tar_copy "${ROOT_DIR}/tests" "${BE_DIR}/tests" --exclude='bin' --exclude='obj'
mkdir -p "${BE_DIR}/scripts"
for f in setup-dev.sh setup-dev.ps1 dev-up.sh dev-up.ps1 dev-down.sh smoke-api.sh smoke-test.sh \
  test-backend.sh test-backend-host.sh test-backend.ps1 test-ia-external.sh reset-test-databases.sh \
  export-openapi.sh export-openapi.ps1; do
  if [[ -e "${ROOT_DIR}/scripts/$f" ]]; then
    cp -a "${ROOT_DIR}/scripts/$f" "${BE_DIR}/scripts/"
  fi
done
if [[ -d "${ROOT_DIR}/scripts/lib" ]]; then
  cp -a "${ROOT_DIR}/scripts/lib" "${BE_DIR}/scripts/"
fi

cp "${ROOT_DIR}/docker-compose.yml" "${BE_DIR}/"
cp "${ROOT_DIR}/.env.example" "${BE_DIR}/"
cp "${ROOT_DIR}/Directory.Packages.props" "${BE_DIR}/" 2>/dev/null || true
mkdir -p "${BE_DIR}/exemplos/postman"
cp -a "${ROOT_DIR}/deliverables/postman/." "${BE_DIR}/exemplos/postman/"
mkdir -p "${BE_DIR}/docs/sprint2"
for doc in ENDPOINTS.md OPENAPI_COMPARACAO.md CONTRATO_API.md openapi.yaml IA_GEMINI.md COMO_TESTAR.md STATUS.md CHECKLIST_ENTREGA.md APRESENTACAO.md; do
  cp "${ROOT_DIR}/docs/sprint2/$doc" "${BE_DIR}/docs/sprint2/" 2>/dev/null || true
done

cat > "${BE_DIR}/README_ENTREGA.md" <<EOF
# InovaGAB Backend — Pacote Sprint 2

Gerado em ${DATE_UTC} · commit ${COMMIT_SHORT}

## Pré-requisitos

- Docker Compose, .NET SDK 8.0.425
- Bash ou PowerShell

## Passos (ordem)

1. \`cp .env.example .env\` ou \`bash scripts/setup-dev.sh\` (na raiz deste pacote)
2. \`bash scripts/dev-up.sh\` ou \`docker compose up -d --build\`
3. \`bash scripts/smoke-api.sh\`
4. Swagger: http://127.0.0.1:8080/swagger

Contas seed: \`*@inovagab.local\` (senhas em \`.env\` DEV_PASSWORD_*).

Sem SQL migrations — apenas MongoDB + seed idempotente.
EOF

( cd "${STAGING}" && zip -rq "$BACKEND_ZIP" "InovaGAB_Backend_Sprint2" )

# --- Android package ---
AND_DIR="${STAGING}/InovaGAB_Android_Sprint2"
mkdir -p "$AND_DIR"

tar_copy "${ROOT_DIR}/app" "${AND_DIR}/app" --exclude='build'

for item in gradlew gradlew.bat gradle settings.gradle.kts build.gradle.kts gradle.properties; do
  [[ -e "${ROOT_DIR}/$item" ]] && cp -a "${ROOT_DIR}/$item" "${AND_DIR}/"
done

mkdir -p "${AND_DIR}/apk"
cp "$APK_SRC" "${AND_DIR}/apk/app-debug.apk"
mkdir -p "${AND_DIR}/exemplos/postman"
cp -a "${ROOT_DIR}/deliverables/postman/." "${AND_DIR}/exemplos/postman/" 2>/dev/null || true
cp "${ROOT_DIR}/docs/sprint2/APRESENTACAO.md" "${AND_DIR}/docs_APRESENTACAO.md" 2>/dev/null || true

cat > "${AND_DIR}/README_ENTREGA.md" <<EOF
# InovaGAB Android — Pacote Sprint 2

Gerado em ${DATE_UTC} · commit ${COMMIT_SHORT}

## APK

- \`apk/app-debug.apk\` — debug, **HTTP cleartext** apenas neste build
- **baseURL debug:** \`http://10.0.2.2:8080/\` (emulador → host)
- **Celular físico:** altere \`API_BASE_URL\` em \`app/build.gradle.kts\` (debug) para \`http://<IP-LAN>:8080/\` e recompile; API deve estar acessível na rede

## Build local

\`\`\`bash
./gradlew :app:assembleDebug
\`\`\`

Release usa HTTPS placeholder (\`https://api.inovagab.local/\`) — sem cleartext.

## Demonstração

1. Suba o backend (pacote Backend ou monorepo).
2. Instale o APK: \`adb install -r apk/app-debug.apk\`
3. Login seed gestor/operador/líder (ver backend README).
EOF

( cd "${STAGING}" && zip -rq "$ANDROID_ZIP" "InovaGAB_Android_Sprint2" )

BE_SHA="$(sha256_file "$BACKEND_ZIP")"
AND_SHA="$(sha256_file "$ANDROID_ZIP")"
APK_SHA="$(sha256_file "$APK_SRC")"

cat > "$MANIFEST" <<EOF
# Manifesto de entrega Sprint 2

| Campo | Valor |
|-------|-------|
| Data UTC | ${DATE_UTC} |
| Git commit | ${COMMIT} |
| .NET SDK (host build) | ${DOTNET_VER} |
| Mongo (Compose) | mongo:7.0.24 |
| Modelo IA configurado | gemini-2.0-flash |

## Artefatos

| Arquivo | SHA-256 |
|---------|---------|
| InovaGAB_Backend_Sprint2.zip | ${BE_SHA} |
| InovaGAB_Android_Sprint2.zip | ${AND_SHA} |
| app-debug.apk (fonte) | ${APK_SHA} |

## Roteiro de avaliação

1. Extrair Backend → \`setup-dev.sh\` → \`dev-up.sh\` → \`smoke-api.sh\`
2. Extrair Android → instalar \`apk/app-debug.apk\` com backend em \`127.0.0.1:8080\`
3. Opcional: Postman em \`exemplos/postman/\`
4. Apresentação: ver \`docs/sprint2/APRESENTACAO.md\` (PDF/PPT pendente)

## Exclusões do ZIP

.git, .env, segredos, bin/obj/build intermediários, local.properties; APK debug incluído por exceção.
EOF

echo "==> Validar extração em /tmp"
VAL_ROOT="$(mktemp -d)"
unzip -q "$BACKEND_ZIP" -d "$VAL_ROOT"
test -f "${VAL_ROOT}/InovaGAB_Backend_Sprint2/docker-compose.yml"
test -f "${VAL_ROOT}/InovaGAB_Backend_Sprint2/backend/InovaGAB.sln" || test -f "${VAL_ROOT}/InovaGAB_Backend_Sprint2/backend/src/InovaGAB.Api/InovaGAB.Api.csproj"

unzip -q "$ANDROID_ZIP" -d "$VAL_ROOT"
test -f "${VAL_ROOT}/InovaGAB_Android_Sprint2/apk/app-debug.apk"
test -f "${VAL_ROOT}/InovaGAB_Android_Sprint2/gradlew"

echo "==> Build limpo backend (extraído)"
EX_BE="${VAL_ROOT}/InovaGAB_Backend_Sprint2/backend"
if [[ -f "${EX_BE}/InovaGAB.sln" ]]; then
  ( cd "$EX_BE" && dotnet restore InovaGAB.sln && dotnet build InovaGAB.sln -c Release )
else
  ( cd "$EX_BE" && dotnet restore && dotnet build -c Release )
fi

rm -rf "$VAL_ROOT"

echo "OK: $BACKEND_ZIP"
echo "OK: $ANDROID_ZIP"
echo "Manifest: $MANIFEST"
