# Matriz de testes — Sprint 2

Legenda de execução:

| Código | Significado |
|---|---|
| **AUTO** | `dotnet test` / `gradlew test` |
| **SMOKE** | `scripts/smoke-api.sh` |
| **MAN** | Roteiro manual |
| **OPT** | Opt-in (`test-ia-external.sh`) |
| **PEND** | Depende de ambiente (emulador, chave IA) |

---

## Backend — suítes automatizadas

| ID | Escopo | Projeto / filtro | Cobertura principal |
|---|---|---|---|
| T-BE-UNIT | Regras ROI/lucro | `InovaGAB.UnitTests` | `RelatorioCalculosTests` |
| T-BE-INT-AUTH | JWT, refresh, replay, logout | `IntegrationTests/Auth` | Roles, JSON sem hash |
| T-BE-INT-EST | Estratégias + ideias | `EstrategiasIdeiasTests` | Vigência, histórico, pontos, isolamento op2 |
| T-BE-INT-PRJ | Projetos, relatórios, ranking | `ProjetosRelatoriosRankingTests` | Conversão 1x, DELETE, dashboard, ROI null |
| T-BE-INT-IA-MOCK | IA com fake Gemini | `IdeiaAnalysisTests` | 503/201, histórico, desatualizada |
| T-BE-E2E | Jornada HTTP única | `E2E/Sprint2JornadaHttpE2ETests` | Líder→op1→op2→gestor→dashboard→ranking |
| T-BE-IA-REAL | Gemini real | `InovaGAB.IaExternalTests` | **OPT** — falha se rodar sem `AI_API_KEY` |

Mongo: **replica set real** (`docker compose`), sem InMemory/SQLite.

---

## Backend — mapa requisito → teste

| Requisito | Teste AUTO | Notas |
|---|---|---|
| CRUD estratégias (Líder) | T-BE-INT-EST, T-BE-E2E | |
| CRUD ideias (operador) | T-BE-INT-EST | PUT/DELETE com versão em outros fluxos |
| Mass assignment ideia | T-BE-INT-PRJ | POST projeto com `ideiaId` inválido → 400 |
| Paginação | Parcial em listagens nos testes de projeto | |
| JWT / refresh | T-BE-INT-AUTH | |
| Histórico estratégia | T-BE-INT-EST | GET `/historico` |
| Exclusão lógica If-Match | T-BE-INT-PRJ | DELETE projeto |
| Concorrência 409 | T-BE-INT-EST, T-BE-E2E | Conversão duplicada |
| Rollback transacional | T-BE-INT-PRJ | Conversão + estado ideia |
| Dashboard / ROI null | T-BE-INT-PRJ, T-BE-UNIT | |
| IA sem chave | T-BE-INT-IA-MOCK, Postman 503 | Não verde falso em IaExternal |

---

## Android

| ID | Tipo | Arquivo | AUTO |
|---|---|---|---|
| T-AND-UNIT-MOSHI | Unit | `MoneyMoshiTest` | Sim |
| T-AND-UNIT-API-MSG | Unit | `ApiMessagesTest` | Sim |
| T-AND-LINT | Lint | `lintDebug` | Sim |
| T-AND-APK | Build | `assembleDebug` | Sim |
| T-AND-INST-SMOKE | Instrumentado | `LoginSmokeInstrumentedTest` | PEND (API + emulador) |
| T-AND-COMPOSE-E2E | UI | TestTags + roteiro MAN | PEND |

### Roteiro manual por perfil (resultado esperado)

| Perfil | Passos | Esperado |
|---|---|---|
| Operador | Login → nova ideia (estratégia vigente) → Minhas ideias | Ideia `ENVIADA`; ranking visível |
| Operador2 | Login → tentar abrir ideia do op1 (API) | 404/negado |
| Gestor | Gestão → aprovar → Analisar IA → converter projeto | Status `APROVADA` → `VIROU_PROJETO`; IA 503 sem chave com mensagem real |
| Líder | Orientação CRUD → Dashboard filtros | Gráficos com séries API ou vazio honesto |
| Todos | Ranking | Sem e-mail; ordem por pontos |

---

## Scripts (exit ≠ 0 em falha)

| Script | Função |
|---|---|
| `scripts/setup-dev.sh` | `.env` local |
| `scripts/dev-up.sh` | API + wait ready |
| `scripts/smoke-api.sh` | Health + login |
| `scripts/test-backend.sh` | Compose test-runner |
| `scripts/test-backend-host.sh` | dotnet no host |
| `scripts/test-ia-external.sh` | IA opt-in (exit 2 sem chave) |
| `scripts/test-android.sh` | Gradle test/lint/APK |
| `scripts/reset-test-databases.sh` | Só `inovagab_test*` + `CONFIRM_RESET_TEST_DB=yes` |
| `scripts/collect-evidence.sh` | Manifesto de evidência |

---

## Registro de execução (agente Cloud)

Preencher após cada rodada local/CI:

| Campo | Valor |
|---|---|
| Data UTC | _(executar `collect-evidence.sh`)_ |
| Commit | `git rev-parse --short HEAD` |
| Comando backend | `bash scripts/test-backend.sh` |
| Comando Android | `bash scripts/test-android.sh` |
| Testes passando | _(contagem TRX / relatório Gradle)_ |
| Falhas | _(listar ou "nenhuma")_ |
| IA real | PEND / executado com chave |
