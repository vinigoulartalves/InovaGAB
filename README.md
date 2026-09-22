# InovaGAB — Sprint 2 (entrega FIAP)

Plataforma mobile de inovação corporativa (FIAP + Grupo Águia Branca): app **Android** (Kotlin/Compose) consumindo API **.NET 8** com **MongoDB** (EF Core) e **IA Gemini** opcional para análises de ideias.

| Sprint | Escopo |
|--------|--------|
| **1** | Android + Firebase (preservado no histórico do repositório) |
| **2** | API REST + MongoDB + app via Retrofit + testes automatizados |

---

## Identificação da entrega

Preencher antes de submeter na FIAP:

| Campo | Valor |
|-------|-------|
| Integrantes | `[NOME]` — RM `[RM]` |
| Disciplina / turma | _(conforme enunciado)_ |
| Repositório | _(URL se aplicável)_ |

---

## Estrutura do repositório

| Pasta | Conteúdo |
|-------|----------|
| `app/` | Android (Compose, Retrofit; perfis OPERADOR, GESTOR, LIDER) |
| `backend/` | API modular (`Api`, `Application`, `Domain`, `Infrastructure`) |
| `tests/` | Unit, integração Mongo, E2E HTTP, IA externa opt-in |
| `infra/` | Init replica set MongoDB (`rs0`) |
| `scripts/` | Setup, Docker, testes, empacotamento |
| `deliverables/postman/` | **Collection Postman** da API Sprint 2 |
| `deliverables/` | ZIPs gerados por `package-delivery` (não versionados) |

---

## Pré-requisitos

| Ferramenta | Versão / nota |
|------------|----------------|
| .NET SDK | **8.0.425** (`backend/global.json`) |
| Docker Compose | Mongo **7.0.24** + API (perfil dev/testes) |
| JDK | **17** (Gradle Android) |
| Android SDK | compile/target **34**, minSdk **24** |
| Bash ou PowerShell | Scripts equivalentes onde existir |
| Postman ou Insomnia | Importar collection em `deliverables/postman/` |

**IA (opcional, Plus):** chave Google AI Studio (`AI_API_KEY`), modelo padrão `gemini-2.0-flash`. O core da API **não** depende de IA.

---

## Configuração inicial

Na **raiz** do repositório, crie o `.env` (não versionado):

```bash
bash scripts/setup-dev.sh
# ou: cp .env.example .env e preencher manualmente
```

| Variável | Uso |
|----------|-----|
| `JWT_SECRET` | Assinatura JWT |
| `SEED_ENABLED` | Seed demo no Mongo (`*@inovagab.local`) |
| `DEV_PASSWORD_*` | Senhas das contas seed (geradas pelo setup) |
| `AI_ENABLED`, `AI_API_KEY`, `AI_MODEL` | Análises Gemini (gestor) |

**Contas demo (após seed):**

| E-mail | Perfil |
|--------|--------|
| `operador1@inovagab.local` | OPERADOR |
| `operador2@inovagab.local` | OPERADOR |
| `gestor@inovagab.local` | GESTOR |
| `lider@inovagab.local` | LIDER |

Senhas: valores `DEV_PASSWORD_OPERADOR1`, `DEV_PASSWORD_OPERADOR2`, `DEV_PASSWORD_GESTOR`, `DEV_PASSWORD_LIDER` no `.env`.

**Persistência:** EF Core + `MongoDB.EntityFrameworkCore` — **sem** SQL Server e **sem** migrations SQL. Transações exigem replica set (`docker compose`).

**Seed vs Firebase:** Sprint 2 usa MongoDB com dados sintéticos. Firebase da Sprint 1 **não** é migrado nem apagado pelo backend.

---

## Executar o projeto

### Ordem recomendada (Bash)

```bash
bash scripts/setup-dev.sh
bash scripts/dev-up.sh              # mongo rs0 + api → http://127.0.0.1:8080
bash scripts/smoke-api.sh           # health + login seed
```

### PowerShell (Windows)

```powershell
.\scripts\setup-dev.ps1
.\scripts\dev-up.ps1
bash scripts/smoke-api.sh           # ou curl em /health/ready
```

### URLs

| Serviço | URL |
|---------|-----|
| API | `http://127.0.0.1:8080` |
| Swagger (Development) | `http://127.0.0.1:8080/swagger` |
| Health | `/health/live`, `/health/ready` |

`docker-compose.yml` é **somente avaliação local** — Mongo não é exposto publicamente.

### Backend sem Docker

1. MongoDB acessível (replica set ou `directConnection=true` em nó único).
2. Exportar `Mongo__ConnectionString`, `Mongo__DatabaseName`, `Jwt__Secret` (ou `.env` na raiz).
3. `cd backend && dotnet run --project src/InovaGAB.Api`

Build/teste local:

```bash
cd backend
dotnet restore InovaGAB.sln
dotnet build InovaGAB.sln -c Release
dotnet test InovaGAB.sln -c Release
```

### Android

| Build | `API_BASE_URL` |
|-------|----------------|
| **debug** | `http://10.0.2.2:8080/` (emulador → localhost do host) |
| **release** | `https://api.inovagab.local/` — **sem** cleartext HTTP |

**Celular físico:** use o IP LAN do PC (ex. `http://192.168.1.10:8080/`) em `app/build.gradle.kts` (`buildTypes.debug`) e recompile; a API deve escutar na interface acessível na rede.

APK debug: `app/build/outputs/apk/debug/app-debug.apk`

```bash
bash scripts/test-android.sh        # unit + lint + assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Collection da API (Postman)

Arquivos versionados:

| Arquivo | Descrição |
|---------|-----------|
| `deliverables/postman/InovaGAB-Sprint2.postman_collection.json` | Requests Auth, estratégias, ideias, projetos, relatórios |
| `deliverables/postman/InovaGAB-Sprint2.postman_environment.template.json` | Template de ambiente local |

**Importar:**

1. Suba a API (`bash scripts/dev-up.sh`).
2. No Postman: **Import** → collection + environment template.
3. Duplique o environment e preencha `senhaOperador1`, `senhaGestor`, `senhaLider` com os valores do `.env`.
4. Execute **Auth → Login** (tokens são gravados no environment pelos scripts de teste da collection).

Contrato interativo alternativo: **Swagger** em Development (`/swagger`).

Export OpenAPI a partir da API em execução (opcional):

```bash
bash scripts/export-openapi.sh      # gera deliverables/openapi-runtime.json
```

---

## Referência de endpoints

**Base:** `http://127.0.0.1:8080` · **Prefixo:** `/api/v1` · **Auth:** `Authorization: Bearer {accessToken}` (exceto login/refresh/logout anônimo e health)

Perfis: `OPERADOR`, `GESTOR`, `LIDER`.

### Health (sem JWT)

| Método | Rota |
|--------|------|
| GET | `/health/live` |
| GET | `/health/ready` |

### Auth

| Método | Rota | Notas |
|--------|------|-------|
| POST | `/api/v1/auth/login` | `{ "email", "senha" }` |
| POST | `/api/v1/auth/refresh` | `{ "refreshToken" }` |
| POST | `/api/v1/auth/logout` | `{ "refreshToken" }` |
| GET | `/api/v1/auth/me` | JWT |
| GET | `/api/v1/usuarios/responsaveis` | GESTOR — gestores ativos |

### Estratégias (LIDER altera; todos consultam)

| Método | Rota |
|--------|------|
| GET/POST | `/api/v1/estrategias` |
| GET/PUT/DELETE | `/api/v1/estrategias/{id}` |
| GET | `/api/v1/estrategias/{id}/historico` |

DELETE exige header **`If-Match`** (versão). Vigência em datas civis; “hoje” em `America/Sao_Paulo`.

### Ideias

| Método | Rota | Perfil principal |
|--------|------|------------------|
| GET/POST | `/api/v1/ideias` | OPERADOR cria |
| GET/PUT/DELETE | `/api/v1/ideias/{id}` | OPERADOR (autor) |
| PATCH | `/api/v1/ideias/{id}/avaliacao` | GESTOR |
| POST | `/api/v1/ideias/{id}/projeto` | GESTOR — conversão |
| POST/GET | `/api/v1/ideias/{id}/analises-ia` | GESTOR — IA opcional |

### Projetos

| Método | Rota |
|--------|------|
| GET/POST | `/api/v1/projetos` |
| GET/PUT/DELETE | `/api/v1/projetos/{id}` |

OPERADOR recebe **403** na listagem. DELETE com **`If-Match`**.

### Relatórios e ranking

| Método | Rota | Perfil |
|--------|------|--------|
| GET | `/api/v1/relatorios/dashboard` | LIDER |
| GET | `/api/v1/relatorios/estrategias` | LIDER |
| GET | `/api/v1/relatorios/projetos/{id}` | LIDER |
| GET | `/api/v1/ranking` | JWT (todos) |

Erros: RFC 7807 ProblemDetails + campo `code` (ex.: `CREDENCIAIS_INVALIDAS`, `CONCORRENCIA`, `IA_INDISPONIVEL`).

---

## Backend — stack fixada

| Componente | Versão |
|------------|--------|
| SDK .NET | 8.0.425 |
| `Microsoft.EntityFrameworkCore` | 8.0.30 |
| `MongoDB.EntityFrameworkCore` | 8.4.4 |
| Imagem Mongo (Compose) | `mongo:7.0.24` |

Projetos: `InovaGAB.Api` (HTTP/Swagger), `Application`, `Domain`, `Infrastructure` (EF `InovaGabDbContext`).

**Segurança:** senhas com `PasswordHasher<Usuario>`; hash nunca exposto na API. JWT ~15 min; logout revoga refresh (sem blacklist de access token nesta versão).

**IA (gestor):** `POST /api/v1/ideias/{id}/analises-ia` com Gemini quando `AI_ENABLED=true` e `AI_API_KEY` definida; caso contrário **503** `IA_INDISPONIVEL`.

---

## Testes

```bash
bash scripts/test-backend.sh        # integração (profile tests, Docker)
bash scripts/test-android.sh
bash scripts/test-ia-external.sh      # exit 2 sem AI_API_KEY (esperado)
dotnet test tests/InovaGAB.UnitTests
```

CI: `.github/workflows/ci.yml` (backend Mongo + Android lint/unit + jornadas instrumentadas).

---

## Empacotamento para entrega

Gera ZIPs em `deliverables/`:

```bash
bash scripts/package-delivery.sh
# ou: .\scripts\package-delivery.ps1
```

| Artefato | Conteúdo |
|----------|----------|
| `InovaGAB_Backend_Sprint2.zip` | backend, tests, infra, scripts, compose, `.env.example`, Postman, **README** |
| `InovaGAB_Android_Sprint2.zip` | fontes Gradle + `apk/app-debug.apk` + Postman + **README** |

Exclui `.git`, `.env`, segredos, `bin/obj`; inclui APK debug. Após gerar, o script imprime **SHA-256** dos ZIPs no terminal.

**Roteiro de avaliação (ZIP):**

1. Extrair Backend → `bash scripts/setup-dev.sh` → `bash scripts/dev-up.sh` → `bash scripts/smoke-api.sh`
2. Importar Postman de `exemplos/postman/` (ou `deliverables/postman/` no monorepo)
3. Instalar `apk/app-debug.apk` com backend em `127.0.0.1:8080` (emulador: `10.0.2.2:8080`)

**Não** publicar automaticamente na FIAP a partir deste repositório.

### Checklist antes de submeter

- [ ] Nomes e RMs preenchidos neste README
- [ ] `bash scripts/smoke-api.sh` OK com `.env` local
- [ ] Postman: login + pelo menos um fluxo gestor/líder
- [ ] APK debug instalado e login seed no emulador/dispositivo
- [ ] Apresentação **PDF/PPT** conforme enunciado (fora deste repositório, se exigido)
- [ ] ZIPs gerados e hashes anotados para o formulário de entrega

---

## Troubleshooting

| Problema | Ação |
|----------|------|
| API `503` ready | Aguardar init replica set; `docker compose logs mongo-init api` |
| Compose trava em `mongo-init Waiting` | 1–2 min na 1ª subida; se persistir: `docker compose down -v` e `bash scripts/dev-up.sh` |
| Login 401 | Conferir `.env` e senhas `DEV_PASSWORD_*`; reiniciar API (`docker compose up -d --build api`) ou `docker compose down -v` |
| Índices Mongo conflitantes | `docker compose down -v` ou rebuild API após `git pull` |
| Android não conecta | Emulador + build **debug**; backend em `127.0.0.1:8080` |
| `IA_INDISPONIVEL` | Esperado sem chave; `AI_ENABLED=true` + `AI_API_KEY` |
| `test-backend.sh` falha | Docker em execução |
| SDK mismatch | Instalar .NET **8.0.425** |

---

## Versões backend (referência Docker)

| Variável env | Seção .NET |
|--------------|------------|
| `JWT_SECRET` | `Jwt__Secret` |
| `Mongo__ConnectionString` | `Mongo:ConnectionString` |
| `Mongo__DatabaseName` | `Mongo:DatabaseName` |
| `AI__Enabled` / `AI__ApiKey` | `AI:Enabled` / `AI:ApiKey` |
| `SEED_ENABLED` | `Seed__Enabled` (desligado em Production) |
