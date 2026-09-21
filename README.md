# InovaGAB

Plataforma mobile de inovação corporativa (FIAP + Grupo Águia Branca).

| Sprint | Escopo |
|--------|--------|
| **1** | Android Kotlin/Compose + Firebase (preservado no histórico) |
| **2** | API .NET 8 + MongoDB (EF Core) + app REST + IA Gemini opcional |

## Estrutura do repositório

| Pasta | Conteúdo |
|-------|----------|
| `app/` | Android (Compose, Retrofit, perfis OPERADOR/GESTOR/LIDER) |
| `backend/` | API modular (Api, Application, Domain, Infrastructure) |
| `tests/` | Unit, integração Mongo, E2E HTTP, IA externa opt-in |
| `infra/` | Init replica set MongoDB (`rs0`) |
| `scripts/` | Setup, Docker, testes, empacotamento entrega |
| `docs/sprint2/` | Contrato, endpoints, STATUS, apresentação, checklist |
| `deliverables/` | Postman, ZIPs gerados (`package-delivery`) |

## Pré-requisitos

| Ferramenta | Versão / nota |
|------------|----------------|
| .NET SDK | **8.0.425** (`backend/global.json`) |
| Docker Compose | API + Mongo 7.0.24 (perfil dev/testes) |
| JDK | **17** (Android Gradle) |
| Android SDK | compile/target 34, minSdk 24 |
| Bash ou PowerShell | Scripts equivalentes onde existir |

**IA (Plus, opcional):** `AI_API_KEY` Google AI Studio, modelo `gemini-2.0-flash` — core da API **não** depende de IA.

## Configuração `.env`

Na **raiz** do repositório (não versionar):

```bash
bash scripts/setup-dev.sh    # cria .env com JWT_SECRET e senhas seed
# ou: cp .env.example .env e preencher manualmente
```

| Variável | Uso |
|----------|-----|
| `JWT_SECRET` | Assinatura JWT |
| `SEED_ENABLED` | Seed demo Mongo (`*@inovagab.local`) |
| `DEV_PASSWORD_*` | Senhas das contas seed |
| `AI_ENABLED`, `AI_API_KEY`, `AI_MODEL` | Análises Gemini (gestor) |

**Seed vs Firebase:** Sprint 2 usa **novo MongoDB** com dados sintéticos. Firebase da Sprint 1 não é migrado nem apagado pelo backend.

**Persistência:** EF Core + `MongoDB.EntityFrameworkCore` — **sem** SQL Server e **sem** migrations SQL. Transações exigem replica set (`docker compose`).

## Comandos — ordem recomendada (Bash)

```bash
bash scripts/setup-dev.sh
bash scripts/dev-up.sh              # mongo rs0 + api → http://127.0.0.1:8080
bash scripts/smoke-api.sh           # health + login seed
bash scripts/test-backend.sh        # integração (profile tests)
bash scripts/test-android.sh        # unit + lint + APK debug
bash scripts/export-openapi.sh      # com API no ar
bash scripts/package-delivery.sh    # ZIPs em deliverables/
```

## Comandos — PowerShell (Windows)

```powershell
.\scripts\setup-dev.ps1
.\scripts\dev-up.ps1
bash scripts/smoke-api.sh          # ou curl manual em /health/ready
.\scripts\test-backend.ps1
bash scripts/test-android.sh       # requer Gradle no PATH
.\scripts\export-openapi.ps1
.\scripts\package-delivery.ps1
```

## URLs e Swagger

| Serviço | URL |
|---------|-----|
| API (host) | `http://127.0.0.1:8080` |
| Swagger | `http://127.0.0.1:8080/swagger` (Development) |
| Health | `/health/live`, `/health/ready` |

`docker-compose.yml` é **somente avaliação local** — Mongo não exposto publicamente.

## Android — baseURL

| Build | `API_BASE_URL` |
|-------|----------------|
| **debug** | `http://10.0.2.2:8080/` (emulador → localhost do host) |
| **release** | `https://api.inovagab.local/` — **sem** cleartext HTTP |

**Celular físico:** use o IP LAN do PC (ex. `http://192.168.1.10:8080/`) no `buildTypes.debug` e recompile; backend deve escutar na interface acessível (não apenas 127.0.0.1) para demo em rede.

APK debug: `app/build/outputs/apk/debug/app-debug.apk` ou pacote `deliverables/InovaGAB_Android_Sprint2.zip`.

## Testes e documentação

- [`docs/sprint2/COMO_TESTAR.md`](docs/sprint2/COMO_TESTAR.md)
- [`docs/sprint2/MATRIZ_TESTES.md`](docs/sprint2/MATRIZ_TESTES.md)
- [`docs/sprint2/ENDPOINTS.md`](docs/sprint2/ENDPOINTS.md)
- [`docs/sprint2/CHECKLIST_ENTREGA.md`](docs/sprint2/CHECKLIST_ENTREGA.md)
- [`docs/sprint2/STATUS.md`](docs/sprint2/STATUS.md)

## Empacotamento entrega FIAP (preparação local)

Gera `deliverables/InovaGAB_Backend_Sprint2.zip` e `InovaGAB_Android_Sprint2.zip` + `MANIFEST_ENTREGA.md` (commit, SHA-256). Exclui `.git`, `.env`, `bin/obj`, segredos; inclui APK debug.

**Não** publicar automaticamente na FIAP a partir deste repositório.

## Troubleshooting

| Problema | Ação |
|----------|------|
| API `503` ready | Aguardar init replica set; `docker compose logs mongo-init api` |
| Compose trava em `mongo-init Waiting` | Aguardar 1–2 min na 1ª subida; ver logs (`PRIMARY elected`). Se persistir: `docker compose down -v` e `bash scripts/dev-up.sh` |
| Login 401 | Conferir `.env` e contas `*@inovagab.local` (senhas `DEV_PASSWORD_*` do `setup-dev.sh`) |
| API cai no startup / seed | Após atualizar o repo: `docker compose down -v` (recria índices Mongo alinhados ao EF) |
| Android não conecta | Emulador: backend em `127.0.0.1:8080`; usar build **debug** |
| `IA_INDISPONIVEL` | Esperado sem chave; definir `AI_ENABLED=true` e `AI_API_KEY` |
| `test-backend.sh` falha | Docker em execução; ver `COMO_TESTAR.md` |
| SDK mismatch | Instalar .NET **8.0.425**; imagem Docker API usa SDK 8.0.425 |

Detalhes backend: [`backend/README.md`](backend/README.md).
