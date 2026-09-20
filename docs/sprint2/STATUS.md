# InovaGAB Sprint 2 — STATUS

**Atualizado em:** 2026-09-20 (UTC)  
**Etapa atual:** Consolidação na `main` (PR aberto a partir de `cursor/sprint2-consolidate-main-befe`)  
**Linha integrada:** `origin/cursor/sprint2-testing-befe` (PRs #14–#17)

---

## 1. Documentação e entrega

| Item | Estado |
|------|--------|
| README raiz + `backend/README.md` (pré-requisitos, PS/Bash, EF/Mongo, seed, troubleshooting) | **Implementado** |
| `ENDPOINTS.md` (rota/método/role/erros) | **Implementado** |
| `OPENAPI_COMPARACAO.md` + `export-openapi.sh/.ps1` | **Implementado** (export runtime depende de API local) |
| `APRESENTACAO.md` | **Implementado** (`[NOME]`/`[RM]` pendentes) |
| PDF/PPT apresentação | **Pendente** |
| `CHECKLIST_ENTREGA.md` | **Implementado** |
| `package-delivery.sh/.ps1` + `MANIFEST_ENTREGA.md` | **Implementado** — executar para comprovar ZIP/SHA256 |
| Divergência Dockerfile SDK 8.0.401 → **8.0.425** | **Corrigido** |
| Swagger `Program.cs` descrição desatualizada | **Corrigido** |

---

## 2. Testabilidade (etapa 9)

| Item | Estado |
|------|--------|
| Scripts Bash/PowerShell, Compose `tests`, matriz, CI | **Implementado** |
| `dotnet test` unit (4 ROI) | **Executado** — passed |
| `test-android.sh` | **Executado** — OK |
| `test-backend.sh` | **Não executado** no agente (sem Docker) |

Branch: `cursor/sprint2-testing-befe` (PR #16).

---

## 3. Android etapa 8

REST completo (operador/gestor/líder), dashboard Canvas, IA/conversão — branch `cursor/sprint2-android-features-befe` (PR #15).

---

## 4. Backend etapas 2–6

Auth, estratégias, ideias, projetos, relatórios, ranking, IA Gemini — branch `cursor/sprint2-backend-foundation-befe`.

---

## 5. Evidências IA

`docs/sprint2/IA_EVIDENCIA.md` — chamada real Gemini **PENDENTE** sem `AI_API_KEY` opt-in. Modelo documentado: `gemini-2.0-flash`.

---

## 6. Próximos passos (humano)

1. Preencher `[NOME]` e `[RM]` em `APRESENTACAO.md`.  
2. Exportar PDF/PPT da apresentação.  
3. Rodar `bash scripts/package-delivery.sh` e anexar ZIPs à entrega.  
4. Opcional: `export-openapi.sh` após `dev-up` e atualizar comparação.  
5. Teste IA real + atualizar `IA_EVIDENCIA.md`.  
6. **Não** publicar na FIAP a partir deste agente.
