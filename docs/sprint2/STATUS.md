# InovaGAB Sprint 2 — STATUS

**Atualizado em:** 2026-09-20 (UTC)  
**Etapa atual:** 9 — Testabilidade (scripts, CI, matriz)  
**Branch:** `cursor/sprint2-testing-befe`

---

## 1. Testabilidade (checkout limpo, sem Android Studio para API)

| Item | Estado |
|---|---|
| Scripts Bash (`setup`, `dev-up`, `smoke-api`, `test-backend`, `test-android`, `reset-test-databases`, `collect-evidence`) | **Implementado** |
| Scripts PowerShell (`setup-dev`, `dev-up`, `test-backend`) | **Implementado** |
| `docker compose` profile `tests` + `test-runner` (sem socket Docker no runner) | **Implementado** |
| Wait readiness com timeout (`scripts/lib/wait-for-http.sh`) | **Implementado** |
| Reset banco só `inovagab_test*` com `CONFIRM_RESET_TEST_DB=yes` | **Implementado** |
| TRX/cobertura em `artifacts/` (gitignored) | **Implementado** |
| `InovaGAB.UnitTests` (ROI/lucro) | **Implementado** |
| `Sprint2JornadaHttpE2ETests` | **Implementado** |
| IA real opt-in (`test-ia-external.sh`, assert sem chave) | **Implementado** |
| Postman collection + environment template | **Implementado** |
| `COMO_TESTAR.md` / `MATRIZ_TESTES.md` | **Implementado** |
| GitHub Actions `.github/workflows/ci.yml` | **Adicionado** (não implica pipeline verde no remoto até rodar) |
| Android `ApiMessagesTest` + instrumentado smoke | **Implementado** |

### Execução registrada (agente Cloud, 2026-09-20)

| Comando | Resultado |
|---|---|
| `dotnet test tests/InovaGAB.UnitTests` | **4 passed** |
| `bash scripts/test-android.sh` | **OK** (unit 5 tests incl. ApiMessages, lint, APK) |
| `bash scripts/test-backend.sh` | **Não executado** — Docker indisponível no agente |
| Commit | `$(git rev-parse --short HEAD 2>/dev/null)` |

Integração Mongo: executar localmente `bash scripts/test-backend.sh` ou CI GitHub.

---

## 2. Android etapa 8

Ver branch `cursor/sprint2-android-features-befe` (PR #15). Core library desugaring habilitado para `java.time` em minSdk 24.

---

## 3. Backend etapas 2–6

Auth, estratégias, ideias, projetos, relatórios, ranking, IA — branch `cursor/sprint2-backend-foundation-befe`.

---

## 4. Evidências IA backend

`docs/sprint2/IA_EVIDENCIA.md` — chamada real Gemini **PENDENTE** sem `AI_API_KEY` opt-in.
