# InovaGAB Sprint 2 — STATUS

**Atualizado em:** 2026-09-20 (UTC)  
**Etapa atual:** 5 — Projetos, conversão, relatórios e ranking  
**Branch:** `cursor/sprint2-backend-foundation-befe`

---

## 1. Resumo etapa 5

| Item | Estado |
|---|---|
| CRUD projetos (GESTOR) + consulta (LIDER) | **Implementado** |
| Operador sem acesso a projetos | **Implementado** (403) |
| Criação direta rejeita `ideiaId` | **Implementado** |
| Responsável gestor ativo + estratégia vigente | **Implementado** |
| PUT preserva `ideiaId` / `estrategiaId` | **Implementado** |
| Exclusão lógica + `If-Match` | **Implementado** |
| `POST /ideias/{id}/projeto` transacional | **Implementado** |
| Índice único parcial `ideiaId` em projetos | **Implementado** (driver) |
| Relatórios LIDER (dashboard, estratégias, projeto) | **Implementado** (agregações Mongo) |
| ROI agregado (não média de ROIs); investimento 0 → `null` | **Implementado** |
| Ranking por eventos (sem e-mail) | **Implementado** |
| Seed demo ampliado (A/B relatório) | **Implementado** |
| Testes `ProjetosRelatoriosRankingTests` | **Implementado** — requer Mongo |

---

## 2. Endpoints (etapas 3–5)

- Autenticação e usuários (etapa 3)
- Estratégias e ideias (etapa 4)
- `GET/POST/PUT/DELETE /api/v1/projetos`
- `POST /api/v1/ideias/{id}/projeto`
- `GET /api/v1/relatorios/dashboard`, `/estrategias`, `/projetos/{id}`
- `GET /api/v1/ranking`

---

## 3. Testes (agente Cloud)

```bash
cd backend && dotnet build -c Release && dotnet test ../tests/InovaGAB.IntegrationTests -c Release
```

- **Build:** OK  
- **Testes:** bloqueados sem `MONGODB_URI` (mensagem explícita)

Com Docker: `docker compose --profile tests run --rm test-runner`

---

## 4. Próxima etapa

**Prompt 6+:** análises IA (Gemini), integração Android, CI completo.
