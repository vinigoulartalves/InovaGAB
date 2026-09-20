# Checklist de entrega — Sprint 2 InovaGAB

**Atualizado:** 2026-09-20 (UTC)  
**Não submeter automaticamente na FIAP** — uso interno de preparação.

Legenda: **Comprovado** | **Implementado, não executado** | **Pendente**

---

## Identificação

| Item | Estado |
|------|--------|
| Nome(s) no `APRESENTACAO.md` | **Pendente** — campos `[NOME]` |
| RM(s) | **Pendente** — campos `[RM]` |

---

## Documentação

| Item | Estado |
|------|--------|
| README raiz (pré-requisitos, comandos Bash/PS, troubleshooting) | **Comprovado** (revisão docs entrega) |
| `backend/README.md` (EF/Mongo, seed, Docker, IA) | **Comprovado** |
| `docs/sprint2/ENDPOINTS.md` | **Comprovado** |
| `docs/sprint2/openapi.yaml` vs código | **Comprovado** (análise estática em `OPENAPI_COMPARACAO.md`) |
| Export OpenAPI runtime (`export-openapi.sh`) | **Implementado, não executado** (Docker/API indisponível no agente) |
| `APRESENTACAO.md` | **Comprovado** |
| Apresentação **PDF/PPT** | **Pendente** — exportação não automatizada |
| Diagramas Mermaid (apresentação + docs) | **Comprovado** |

---

## Backend

| Item | Estado |
|------|--------|
| API .NET 8 + Mongo EF | **Comprovado** (código + unit tests) |
| Seed demo vs Firebase legado | **Comprovado** (seed Mongo; Firebase Sprint 1 preservado) |
| Sem migrations SQL | **Comprovado** |
| IA opcional para core | **Comprovado** |
| IA real testada com chave | **Pendente** (`IA_EVIDENCIA.md`) |
| `docker compose` + testes integração | **Implementado, não executado** no agente Cloud |
| Dockerfile SDK alinhado `8.0.425` | **Comprovado** (correção divergência) |

---

## Android

| Item | Estado |
|------|--------|
| Consumo API REST (etapa 8) | **Comprovado** (branch features + merge testing) |
| `assembleDebug` / `test-android.sh` | **Comprovado** no agente |
| APK no pacote de entrega | **Comprovado** após `package-delivery.sh` |
| Release sem HTTP cleartext | **Comprovado** (manifest debug only) |
| baseURL emulador `10.0.2.2` | **Comprovado** (`app/build.gradle.kts`) |

---

## Pacotes ZIP

| Item | Estado |
|------|--------|
| `scripts/package-delivery.sh` | **Comprovado** |
| `scripts/package-delivery.ps1` | **Comprovado** |
| `deliverables/InovaGAB_Backend_Sprint2.zip` | **Comprovado** (agente 2026-09-20; SHA no manifest) |
| `deliverables/InovaGAB_Android_Sprint2.zip` | **Comprovado** |
| `deliverables/MANIFEST_ENTREGA.md` (commit/SHA256) | **Comprovado** |
| Extração + `dotnet build` backend extraído | **Comprovado** |
| Exclusão `.env`, segredos, `bin/obj`, `.git` | **Comprovado** (regras no script) |

---

## Testes / CI

| Item | Estado |
|------|--------|
| Unit backend (4 testes ROI) | **Comprovado** |
| `test-android.sh` | **Comprovado** |
| `test-backend.sh` (Compose) | **Implementado, não executado** (sem Docker) |
| CI GitHub Actions | **Implementado** — status remoto não afirmado |

---

## Destaques para revisão manual

1. **Preencher [NOME] e [RM]** antes da submissão FIAP.  
2. **IA Gemini:** modelo documentado; chamada real **não testada** sem `AI_API_KEY`.  
3. **PDF/PPT:** obrigatório no enunciado; gerar localmente.  
4. **APK:** usar ZIP Android ou artefato `artifacts/android/app-debug.apk` após build.  
5. **Não publicar** repositório nem portais FIAP a partir deste checklist.
