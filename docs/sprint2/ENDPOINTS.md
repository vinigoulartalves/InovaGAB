# InovaGAB Sprint 2 — Endpoints (implementação real)

**Base URL (dev):** `http://127.0.0.1:8080`  
**Prefixo:** `/api/v1`  
**Contrato OpenAPI:** [`openapi.yaml`](./openapi.yaml) · **Tabela humana:** este arquivo  
**Autenticação:** `Authorization: Bearer {accessToken}` (exceto login, refresh, logout anônimo, health)

Perfis: `OPERADOR`, `GESTOR`, `LIDER`. Políticas no servidor (`AuthPolicies`).

---

## Health (sem JWT)

| Método | Rota | Role | Payload | Resposta 2xx | Erros |
|--------|------|------|---------|--------------|-------|
| GET | `/health/live` | — | — | `200` `{ "status": "live" }` | — |
| GET | `/health/ready` | — | — | `200` JSON com `status` e `checks` (Mongo) | `503` se Mongo indisponível |

---

## Auth (`/api/v1/auth`)

| Método | Rota | Role | Payload | Resposta 2xx | Erros |
|--------|------|------|---------|--------------|-------|
| POST | `/login` | Anônimo | `{ "email", "password" }` | `200` `LoginResponse` (access, refresh, expiração, usuário resumo) | `400` VALIDACAO; `401` CREDENCIAIS_INVALIDAS; `429` rate limit |
| POST | `/refresh` | Anônimo | `{ "refreshToken" }` | `200` `LoginResponse` | `401` REFRESH_INVALIDO |
| POST | `/logout` | Anônimo | `{ "refreshToken" }` | `204` | — |
| GET | `/me` | JWT | — | `200` `UsuarioResumo` | `401` NAO_AUTENTICADO |

---

## Usuários (`/api/v1/usuarios`)

| Método | Rota | Role | Payload | Resposta 2xx | Erros |
|--------|------|------|---------|--------------|-------|
| GET | `/responsaveis` | GESTOR | — | `200` lista `ResponsavelResumo` (gestores ativos) | `401`/`403` |

---

## Estratégias (`/api/v1/estrategias`)

| Método | Rota | Role | Payload / query | Resposta 2xx | Erros |
|--------|------|------|-----------------|--------------|-------|
| GET | `/` | JWT (todos perfis) | Query: paginação, filtros (`EstrategiaListQuery`) | `200` `PagedResult<EstrategiaResumo>` | `401` |
| GET | `/{id}` | JWT | — | `200` `EstrategiaDetalhe` | `404` NAO_ENCONTRADO |
| GET | `/{id}/historico` | JWT | `page`, `pageSize` | `200` `PagedResult<EstrategiaHistoricoItem>` | `404` |
| POST | `/` | LIDER | `EstrategiaCreateRequest` | `200` detalhe | `400`/`403`/`409` vigência |
| PUT | `/{id}` | LIDER | `EstrategiaUpdateRequest` | `200` detalhe | `404`/`409` CONCORRENCIA |
| DELETE | `/{id}` | LIDER | Header **`If-Match`**: versão | `204` | `400` If-Match obrigatório; `409` |

Vigência: datas civis; “hoje” em `America/Sao_Paulo` (`IVigenciaClock`).

---

## Ideias (`/api/v1/ideias`)

| Método | Rota | Role | Payload / query | Resposta 2xx | Erros |
|--------|------|------|-----------------|--------------|-------|
| GET | `/` | JWT | `IdeiaListQuery` (filtros por perfil) | `200` `PagedResult<IdeiaResumo>` | `403` OPERADOR-only scope |
| GET | `/{id}` | JWT | — | `200` `IdeiaDetalhe` | `404` |
| POST | `/` | OPERADOR | `IdeiaCreateRequest` | `200` detalhe | `409` ESTRATEGIA_NAO_VIGENTE |
| PUT | `/{id}` | OPERADOR (autor) | `IdeiaUpdateRequest` | `200` | `409` STATUS_INVALIDO / CONCORRENCIA |
| DELETE | `/{id}` | OPERADOR (autor) | **`If-Match`** versão | `204` | `409` status/concorrência |
| PATCH | `/{id}/avaliacao` | GESTOR | `IdeiaAvaliacaoRequest` (status, prioridade, notas) | `200` | `409` TRANSICAO_STATUS_INVALIDA |
| POST | `/{id}/projeto` | GESTOR | `ConversaoIdeiaProjetoRequest` | `201` `ProjetoDetalhe` | `409` conversão duplicada / status |
| POST | `/{id}/analises-ia` | GESTOR | — | `201` `AnaliseIaDetalhe` | `503` IA_INDISPONIVEL; `429` rate limit |
| GET | `/{id}/analises-ia` | GESTOR | `page`, `pageSize` | `200` `PagedResult<AnaliseIaResumo>` | `404` |

---

## Projetos (`/api/v1/projetos`)

| Método | Rota | Role | Payload / query | Resposta 2xx | Erros |
|--------|------|------|-----------------|--------------|-------|
| GET | `/` | JWT (GESTOR/LIDER; OPERADOR → 403) | `ProjetoListQuery` | `200` `PagedResult<ProjetoResumo>` | `403` |
| GET | `/{id}` | GESTOR/LIDER | — | `200` `ProjetoDetalhe` | `404` |
| POST | `/` | GESTOR | `ProjetoCreateRequest` (**sem** `ideiaId`) | `200` detalhe | `400` se `ideiaId` presente |
| PUT | `/{id}` | GESTOR | `ProjetoUpdateRequest` | `200` | `409` CONCORRENCIA |
| DELETE | `/{id}` | GESTOR | **`If-Match`** | `204` | `409` |

---

## Relatórios (`/api/v1/relatorios`) — apenas LIDER

| Método | Rota | Query | Resposta 2xx | Erros |
|--------|------|-------|--------------|-------|
| GET | `/dashboard` | `estrategiaId`, `projetoId`, `inicio`, `fim` (DateOnly) | `200` `DashboardRelatorio` | `403` |
| GET | `/estrategias` | mesmos filtros | `200` lista agregada | `403` |
| GET | `/projetos/{id}` | — | `200` `RelatorioProjetoDetalhe` | `404` |

ROI agregado pode ser `null` quando não aplicável (Android exibe “Não aplicável”).

---

## Ranking (`/api/v1/ranking`)

| Método | Rota | Role | Resposta 2xx |
|--------|------|------|--------------|
| GET | `/` | JWT (todos) | `200` `RankingResponse` (pontos por usuário, sem e-mail) |

---

## Códigos de erro estáveis (amostra)

| code | HTTP típico |
|------|-------------|
| VALIDACAO | 400 |
| CREDENCIAIS_INVALIDAS / REFRESH_INVALIDO / NAO_AUTENTICADO | 401 |
| ACESSO_NEGADO | 403 |
| NAO_ENCONTRADO | 404 |
| CONCORRENCIA, STATUS_INVALIDO, TRANSICAO_STATUS_INVALIDA, ESTRATEGIA_NAO_VIGENTE | 409 |
| IA_INDISPONIVEL | 502/503 |

Corpo: RFC 7807 ProblemDetails + `code`, `traceId`.

---

## Swagger

Em `Development`: `http://127.0.0.1:8080/swagger`  
Exportação runtime: `bash scripts/export-openapi.sh` (API no ar). Comparação: [`OPENAPI_COMPARACAO.md`](./OPENAPI_COMPARACAO.md).
