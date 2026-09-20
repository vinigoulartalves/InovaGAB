# InovaGAB Sprint 2 — STATUS

**Atualizado em:** 2026-09-20 (UTC)  
**Etapa atual:** 2 — Fundação backend e ambiente  
**Branch de trabalho:** `cursor/sprint2-backend-foundation-befe`

---

## 1. Resumo da etapa 2

| Item | Estado | Evidência |
|---|---|---|
| Solution `backend/InovaGAB.sln` (Api/Application/Domain/Infrastructure) | **Implementado** | Build Release OK |
| CPM + lock files | **Implementado** | `/Directory.Packages.props`, `packages.lock.json` por projeto |
| EF Core Mongo + driver (índices idempotentes) | **Implementado** | `InovaGabDbContext`, `MongoIndexInitializer` |
| Swagger, ProblemDetails, traceId, middleware de erros | **Implementado** | `Program.cs`, `ExceptionHandlingMiddleware` |
| Health `/health/live`, `/health/ready` | **Implementado** | Sem dependência de Gemini |
| Docker Compose + Dockerfile + rs0 init | **Implementado** (arquivos) | `docker-compose.yml`, `infra/mongo/init-replica-set.sh` |
| `.env.example`, `setup-dev.sh` / `.ps1` | **Implementado** | `.env` gerado localmente (não versionado) |
| Teste integração EF CRUD | **Implementado, não executado com Mongo** | Ver §4 |
| Módulos de negócio (auth, CRUDs) | **Pendente** | Etapa 3+ |

---

## 2. Versões e comandos executados

| Ferramenta | Versão observada |
|---|---|
| .NET SDK | 8.0.425 |
| `Microsoft.EntityFrameworkCore` | 8.0.30 |
| `MongoDB.EntityFrameworkCore` | 8.4.4 |
| `MongoDB.Driver` | 3.11.2 |

**Comandos (agente Cloud, 2026-09-20):**

```bash
export PATH="$HOME/.dotnet:$PATH"
cd backend && dotnet restore InovaGAB.sln && dotnet build -c Release
dotnet test -c Release --no-build   # falhou: sem MONGODB_URI
bash scripts/setup-dev.sh           # .env criado
docker compose config               # BLOQUEADO: docker não instalado no pod
```

---

## 3. Bloqueios de ambiente

| Bloqueio | Impacto |
|---|---|
| **Docker ausente** no pod do agente | Não foi possível `compose up`, health HTTP real nem teste EF com replica set |
| **MongoDB indisponível** | `MongoEfCoreCrudTests` falhou com mensagem explícita (não foi marcado como sucesso) |

**Validação pendente na sua máquina / CI com Docker:**

```bash
bash scripts/setup-dev.sh
docker compose up -d --build
bash scripts/smoke-test.sh
docker compose --profile tests run --rm test-runner
```

---

## 4. Teste de integração EF (especificação)

Arquivo: `tests/InovaGAB.IntegrationTests/Persistence/MongoEfCoreCrudTests.cs`

Valida quando `MONGODB_URI` está definido:

- CRUD real via `InovaGabDbContext`
- `decimal`, enum, `DateOnly`, `DateTime` UTC
- Concorrência (`DbUpdateConcurrencyException`)
- Transação explícita (`BeginTransactionAsync`) — requer replica set

---

## 5. Decisões mantidas / novas

1. **Identity completo** ainda não adotado; JWT secret obrigatório no startup (`ValidateOnStart`).
2. **Seed** configurável; **desabilitado em Production** via validação de opções.
3. **IA** (`AI:Enabled`) opcional; readiness **não** consulta Gemini.
4. **Compose** documentado como ambiente de **avaliação**, não produção.
5. Mongo **não publicado** no host; API em `127.0.0.1:8080` apenas.
6. Documento técnico `IntegrationProbeDocument` — apenas fundação, não API de negócio.

---

## 6. Próxima etapa

**Prompt 3:** autenticação (`login/refresh/logout/me`), `PasswordHasher`, refresh tokens, seed de usuários dev, testes HTTP reais.

---

## 7. Documentos relacionados

- [`PLANO_EXECUCAO.md`](./PLANO_EXECUCAO.md)
- [`CONTRATO_API.md`](./CONTRATO_API.md)
- [`RASTREABILIDADE.md`](./RASTREABILIDADE.md)
- [`../../backend/README.md`](../../backend/README.md)
- [`../../README.md`](../../README.md)
