# InovaGAB Sprint 2 — STATUS

**Atualizado em:** 2026-09-20 (UTC)  
**Etapa atual:** 6 — Análises IA (Gemini)  
**Branch:** `cursor/sprint2-backend-foundation-befe`

---

## 1. Resumo etapa 6

| Item | Estado |
|---|---|
| `POST/GET /api/v1/ideias/{id}/analises-ia` (GESTOR) | **Implementado** |
| Cliente HTTP Gemini (`generateContent` + JSON Schema) | **Implementado** |
| `AI__Model` configurável (padrão `gemini-2.0-flash`) | **Implementado** |
| Persistência com provedor/modelo/promptVersion/entradaHash/versão | **Implementado** |
| Cache por hash+modelo+prompt (sem re-chamar API) | **Implementado** |
| Flag `desatualizada` após edição da ideia | **Implementado** |
| IA não altera status/prioridade/pontos | **Implementado** |
| 503 sem chave/desabilitada; 429/502/504 sem segredos | **Implementado** |
| Rate limit `ia-analise` (10/min por usuário) | **Implementado** |
| Testes HTTP mock (`IdeiaAnalysisTests`) | **Implementado** — requer Mongo |
| Teste real opt-in (`InovaGAB.IaExternalTests`) | **Implementado** — requer `AI_API_KEY` |
| Evidência chamada real | **PENDENTE** — ver `IA_EVIDENCIA.md` |

Documentação: `docs/sprint2/IA_GEMINI.md`, `docs/sprint2/IA_EVIDENCIA.md`.

---

## 2. Etapas anteriores (resumo)

- **5:** projetos, conversão, relatórios, ranking  
- **4:** estratégias e ideias  
- **3:** autenticação e seed  

---

## 3. Testes (agente Cloud)

```bash
cd backend && dotnet build -c Release && dotnet test ../tests/InovaGAB.IntegrationTests -c Release
```

- **Build:** OK  
- **Integração:** bloqueada sem `MONGODB_URI`  
- **IA real:** `dotnet test ../tests/InovaGAB.IaExternalTests` com `AI_API_KEY` + Mongo

Com Docker: `docker compose --profile tests run --rm test-runner`

---

## 4. Próxima etapa

**Prompt 7+:** integração Android (Retrofit), telas e CI.
