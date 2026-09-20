# InovaGAB Backend (Sprint 2)

API **.NET 8** com **EF Core + MongoDB** (`MongoDB.EntityFrameworkCore` 8.4.4), JWT (fundação), health checks e Docker Compose de avaliação.

## Projetos

| Projeto | Responsabilidade |
|---|---|
| `InovaGAB.Api` | HTTP, Swagger, ProblemDetails, `/health/*` |
| `InovaGAB.Application` | Casos de uso (módulos nas próximas etapas) |
| `InovaGAB.Domain` | Entidades de domínio |
| `InovaGAB.Infrastructure` | `InovaGabDbContext`, driver Mongo, inicialização idempotente |

## Versões fixadas (etapa 2)

| Componente | Versão |
|---|---|
| SDK .NET | 8.0.425 (`global.json`) |
| `Microsoft.EntityFrameworkCore` | 8.0.30 |
| `MongoDB.EntityFrameworkCore` | 8.4.4 |
| `MongoDB.Driver` | 3.11.2 |
| Imagem MongoDB (Compose) | `mongo:7.0.24` |
| Runtime/SDK Docker API | `8.0.401` / `8.0.21` |

Lock files: `packages.lock.json` em cada projeto (CPM em `/Directory.Packages.props`).

## Configuração (binding explícito)

Variáveis de ambiente (exemplo no `.env` gerado por `scripts/setup-dev.sh`):

| Variável | Seção .NET |
|---|---|
| `JWT_SECRET` | `Jwt__Secret` |
| `Mongo__ConnectionString` | `Mongo:ConnectionString` |
| `Mongo__DatabaseName` | `Mongo:DatabaseName` |
| `AI__Enabled` | `AI:Enabled` (opcional; core não depende de Gemini) |
| `AI__ApiKey` | `AI:ApiKey` |
| `SEED_ENABLED` | `Seed__Enabled` (desligado em Production) |

## Comandos

```bash
cd backend
dotnet restore InovaGAB.sln
dotnet build InovaGAB.sln -c Release
dotnet test InovaGAB.sln -c Release
```

Teste EF↔Mongo: `tests/InovaGAB.IntegrationTests/Persistence/MongoEfCoreCrudTests.cs` — requer `MONGODB_URI` (recomendado: `docker compose --profile tests run --rm test-runner`).

## Análises IA (etapa 6, Plus)

- `POST /api/v1/ideias/{id}/analises-ia` — gestor; Gemini real com saída JSON validada.
- `GET /api/v1/ideias/{id}/analises-ia` — histórico paginado.
- Configuração: `docs/sprint2/IA_GEMINI.md` (`AI_ENABLED`, `AI_API_KEY`, `AI_MODEL`).
- Core sobe sem chave; análises retornam **503** `IA_INDISPONIVEL`.

## Projetos, relatórios e ranking (etapa 5)

- `GET/POST/PUT/DELETE /api/v1/projetos` — gestor CRUD; líder consulta; operador 403.
- `POST /api/v1/ideias/{id}/projeto` — conversão transacional (ideia `APROVADA`, estratégia vigente).
- `GET /api/v1/relatorios/*` — apenas **LIDER**; agregações Mongo (`RelatorioRepository`).
- `GET /api/v1/ranking` — pontos por eventos, sem e-mail.
- Seed demo: projetos A/B (`1000/1500` + `2000/2600` → lucro `1100`, ROI agregado ~`36,6667%`).

## Vigência de estratégias (etapa 4)

Datas `inicioVigencia` / `fimVigencia` são **datas civis**. O cálculo de “hoje” usa o fuso **`America/Sao_Paulo`** via `IVigenciaClock` (relógio injetável `TimeProvider` para UTC).

## Autenticação (etapa 3)

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/auth/login` | Email/senha → access + refresh (rate limit por IP) |
| POST | `/api/v1/auth/refresh` | Rotação de refresh (uso único) |
| POST | `/api/v1/auth/logout` | Revoga refresh (idempotente) |
| GET | `/api/v1/auth/me` | Perfil JWT |
| GET | `/api/v1/usuarios/responsaveis` | Gestores ativos (role GESTOR) |

- Senhas: `PasswordHasher<Usuario>` (`Microsoft.Extensions.Identity.Core`), hash **nunca** na API nem em logs.
- JWT: assinatura, issuer, audience, expiração (~15 min). **Access token emitido permanece válido até expirar**; logout revoga apenas o refresh (sem blacklist de access token nesta versão).
- Seed demo idempotente (`Seed:Enabled`, não em Production): contas `*@inovagab.local` com senhas em `DEV_PASSWORD_*` no `.env`.

## Health

- `GET /health/live` — processo
- `GET /health/ready` — Mongo inicializado + ping (sem dependência de IA)

Swagger em Development: `http://localhost:8080/swagger`

Contrato completo: `docs/sprint2/openapi.yaml`

## Docker

Build a partir da raiz do repositório:

```bash
docker compose up -d --build
```

Rede interna: host Mongo `mongo:27017`, replica set `rs0`. API: `http://api:8080` entre containers; host `http://127.0.0.1:8080`.
