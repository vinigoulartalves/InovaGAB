# InovaGAB Sprint 2 — STATUS

**Atualizado em:** 2026-09-20 (UTC)  
**Etapa atual:** 4 — Estratégias e ideias  
**Branch:** `cursor/sprint2-backend-foundation-befe`

---

## 1. Resumo etapa 4

| Item | Estado |
|---|---|
| CRUD estratégias (LIDER) + consulta/histórico (todos) | **Implementado** |
| Histórico imutável atômico com transação | **Implementado** |
| CRUD ideias (OPERADOR) + avaliação (GESTOR) | **Implementado** |
| Líder sem acesso a ideias individuais | **Implementado** (403 list/get) |
| Vigência SP + estratégia vigente no cadastro | **Implementado** |
| Transições status + 409 | **Implementado** |
| Pontuação CADASTRO +10 / PRIMEIRA_APROVACAO +30 atômica | **Implementado** |
| Índice único eventos pontuação | **Implementado** (driver) |
| Testes integração `EstrategiasIdeiasTests` | **Implementado** — requer Mongo |
| Projetos / conversão / ranking API | **Pendente** (etapa 5) |

---

## 2. Endpoints novos

- `GET/POST/PUT/DELETE /api/v1/estrategias`
- `GET /api/v1/estrategias/{id}/historico`
- `GET/POST/PUT/DELETE /api/v1/ideias`
- `PATCH /api/v1/ideias/{id}/avaliacao`

`DELETE` lógico exige `If-Match: W/"{versao}"`.

---

## 3. Testes (agente Cloud)

```bash
cd backend && dotnet build -c Release && dotnet test -c Release
```

- **Build:** OK  
- **Testes:** bloqueados sem `MONGODB_URI` (mensagem explícita)

Com Docker: `docker compose --profile tests run --rm test-runner`

---

## 4. Próxima etapa

**Prompt 5:** projetos, conversão transacional, relatórios e ranking.
