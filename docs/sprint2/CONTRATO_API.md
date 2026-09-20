# InovaGAB Sprint 2 — Contrato da API REST

**Versão do contrato:** 1.0.0 (etapa 1 — especificação; implementação nas etapas 2+)  
**Base URL (desenvolvimento):** `http://localhost:8080`  
**Prefixo de negócio:** `/api/v1`  
**OpenAPI:** [`openapi.yaml`](./openapi.yaml)

---

## 1. Convenções gerais

| Tópico | Regra |
|---|---|
| Formato | JSON, propriedades **camelCase** |
| IDs | `string` |
| Enums | Texto igual ao Android (`ENVIADA`, `OPERADOR`, …) |
| Instantes | ISO 8601 UTC com `Z` (ex.: `2026-09-20T14:30:00Z`) |
| Datas civis | `yyyy-MM-dd` (vigência, prazo de projeto) |
| Dinheiro | Número JSON (não string); precisão decimal no servidor (`decimal` / Decimal128) |
| Autenticação | `Authorization: Bearer {accessToken}` exceto login/refresh |
| Paginação | Query `page` (1-based), `pageSize` (padrão 20, máx. 100); resposta `PagedResult` |
| Ordenação | Query `sort` opcional (ex.: `criadoEm:desc`); estável com desempate por `id` |

### 1.1 Paginação (`PagedResult`)

```json
{
  "items": [],
  "page": 1,
  "pageSize": 20,
  "totalItems": 0,
  "totalPages": 0
}
```

### 1.2 Erros (`ProblemDetails` estendido)

Corpo comum (RFC 7807 + extensões):

| Campo | Tipo | Descrição |
|---|---|---|
| `type` | string | URI ou identificador de tipo |
| `title` | string | Título curto |
| `status` | int | Código HTTP |
| `detail` | string | Mensagem legível |
| `instance` | string | Caminho da requisição |
| `traceId` | string | Correlação de logs |
| `code` | string | Código estável (`VALIDACAO`, `CONCORRENCIA`, …) |
| `errors` | objeto | Mapa campo → mensagens (400) |

| HTTP | Uso |
|---|---|
| 400 | Validação de entrada |
| 401 | Não autenticado / token inválido |
| 403 | Perfil sem permissão |
| 404 | Não encontrado ou não autorizado (sem vazar existência) |
| 409 | Conflito de negócio, concorrência, transição ilegal, conversão duplicada |
| 429 | Rate limit (login, IA) |
| 502/503/504 | Dependência externa (ex.: Gemini indisponível) |

---

## 2. Concorrência e exclusão lógica

### 2.1 Versão (`versao`)

- Presente em respostas de **Estrategia**, **Ideia** e **Projeto**.
- Em `PUT`, `PATCH` de avaliação e `POST` conversão: o cliente envia `versao` que leu na última GET; se divergir → **409** `code: CONCORRENCIA`.

### 2.2 `If-Match` em exclusões lógicas

`DELETE` que realiza exclusão lógica exige:

```http
If-Match: W/"3"
```

onde `3` é a `versao` atual do recurso. Ausência ou valor incorreto → **409** `CONCORRENCIA` ou **412** (implementação pode unificar em 409 conforme guia).

Após exclusão bem-sucedida, o recurso permanece no banco com `excluidaEm` preenchido; **GET** por id autorizado pode retornar o recurso com `excluidaEm` para auditoria; listagens padrão omitam.

### 2.3 Campos de exclusão lógica

| Recurso | Campo resposta | Listagem padrão |
|---|---|---|
| Estrategia | `excluidaEm` (null se ativa no catálogo) | Omite excluídas |
| Ideia | `excluidaEm` | Omite excluídas |
| Projeto | `excluidaEm` | Omite excluídas |

---

## 3. Autenticação

### 3.1 `POST /api/v1/auth/login`

**Body:** `LoginRequest`

```json
{
  "email": "operador1@inovagab.local",
  "senha": "string"
}
```

**201/200:** `LoginResponse`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresAt": "2026-09-20T15:05:00Z",
  "refreshToken": "opaque-random-string",
  "usuario": {
    "id": "usr-001",
    "nome": "Operador Um",
    "email": "operador1@inovagab.local",
    "perfil": "OPERADOR"
  }
}
```

**401:** credenciais inválidas (mensagem genérica). **429:** muitas tentativas.

### 3.2 `POST /api/v1/auth/refresh`

```json
{
  "refreshToken": "opaque-random-string"
}
```

**200:** novo par access/refresh (rotação — token anterior invalidado). **401:** expirado/revogado. **409:** reutilização detectada (sessão comprometida).

### 3.3 `POST /api/v1/auth/logout`

```json
{
  "refreshToken": "opaque-random-string"
}
```

**204:** idempotente; revoga refresh.

### 3.4 `GET /api/v1/auth/me`

**200:** `UsuarioResumo` (sem hash, sem pontos editáveis).

---

## 4. Usuários

### 4.1 `GET /api/v1/usuarios/responsaveis`

**Role:** GESTOR. **200:** lista mínima de gestores ativos `{ id, nome }` para formulário de projeto.

---

## 5. Estratégias

### 5.1 `GET /api/v1/estrategias`

**Roles:** todos autenticados.  
**Query:** `vigente` (bool), `categoria`, `campanha`, `page`, `pageSize`, `sort`.  
**200:** `PagedResult<EstrategiaResumo>` — sem excluídas por padrão.

### 5.2 `GET /api/v1/estrategias/{id}`

Detalhe; estratégias arquivadas/excluídas visíveis se autorizado e referenciadas.

### 5.3 `GET /api/v1/estrategias/{id}/historico`

**200:** `PagedResult<EstrategiaHistoricoItem>` imutável.

### 5.4 `POST /api/v1/estrategias` — LIDER

**Body:** `EstrategiaCreateRequest` (titulo, descricao, categoria, campanha, inicioVigencia, fimVigencia opcional, ativa).

### 5.5 `PUT /api/v1/estrategias/{id}` — LIDER

**Body:** `EstrategiaUpdateRequest` inclui `versao`. Gera histórico.

### 5.6 `DELETE /api/v1/estrategias/{id}` — LIDER

Exclusão lógica; **`If-Match`** obrigatório.

---

## 6. Ideias

### 6.1 `GET /api/v1/ideias`

- OPERADOR: apenas `autorId = sub`.
- GESTOR: todas (filtros: `status`, `prioridade`, `estrategiaId`, `autorId`, paginação).

### 6.2 `GET /api/v1/ideias/{id}`

Proprietário ou gestor.

### 6.3 `POST /api/v1/ideias` — OPERADOR

**Body:** `IdeiaCreateRequest` — `titulo`, `descricao`, `area`, `estrategiaId` (vigente). **Proibido:** autor, status, prioridade, pontos.

### 6.4 `PUT /api/v1/ideias/{id}` — OPERADOR, status ENVIADA

**Body:** `IdeiaUpdateRequest` + `versao`.

### 6.5 `DELETE /api/v1/ideias/{id}` — OPERADOR, ENVIADA

**If-Match** + exclusão lógica.

### 6.6 `PATCH /api/v1/ideias/{id}/avaliacao` — GESTOR

**Body:**

```json
{
  "versao": 2,
  "status": "APROVADA",
  "prioridade": "ALTA",
  "justificativa": "Alto impacto operacional."
}
```

Transições conforme plano; mesma status idempotente; ilegal → 409.

### 6.7 `POST /api/v1/ideias/{id}/projeto` — GESTOR (conversão)

**Body:** `ConversaoIdeiaProjetoRequest` (campos do projeto exceto ideia/estratégia; inclui `versao` da ideia).

**201:** `ProjetoDetalhe`. **409:** estratégia não vigente, ideia não aprovada, duplicidade, concorrência.

**Exemplo completo:** ver seção 12 e `openapi.yaml`.

---

## 7. Projetos

### 7.1 `GET /api/v1/projetos` — GESTOR, LIDER

Filtros: `status`, `estrategiaId`, `responsavelId`, datas de criação.

### 7.2 `GET /api/v1/projetos/{id}`

### 7.3 `POST /api/v1/projetos` — GESTOR

Criação direta; **`ideiaId` ausente ou rejeitado**; `estrategiaId` vigente; `responsavelId` gestor ativo.

### 7.4 `PUT /api/v1/projetos/{id}` — GESTOR

Atualiza dados/resultados; **não** altera `ideiaId`/`estrategiaId` silenciosamente; `versao` obrigatória.

### 7.5 `DELETE /api/v1/projetos/{id}` — GESTOR

Exclusão lógica com **If-Match**.

---

## 8. Relatórios (LIDER)

### 8.1 `GET /api/v1/relatorios/dashboard`

**Query:** `estrategiaId`, `projetoId`, `inicio`, `fim` (datas criação projeto).

**200:** `DashboardRelatorio` — totais, lucro, ROI agregado, média produtividade, contagem atrasados, séries `investimentoRetornoPorEstrategia`, `distribuicaoPorStatus`.

### 8.2 `GET /api/v1/relatorios/estrategias`

Agrupado por estratégia.

### 8.3 `GET /api/v1/relatorios/projetos/{id}`

Resultado individual do projeto.

---

## 9. Ranking

### `GET /api/v1/ranking`

**Roles:** todos. **200:**

```json
{
  "items": [
    { "posicao": 1, "nome": "Operador Um", "pontos": 40 }
  ],
  "atualizadoEm": "2026-09-20T14:00:00Z"
}
```

Sem email.

---

## 10. Análises IA (GESTOR, Plus)

### 10.1 `POST /api/v1/ideias/{id}/analises-ia`

Dispara análise real; persiste resultado.

### 10.2 `GET /api/v1/ideias/{id}/analises-ia`

Histórico paginado de análises.

**Resposta:** `AnaliseIaResposta` com notas 0–100, `prioridadeSugerida`, `justificativa`, `riscos[]`, `melhorias[]`, metadados (`provedor`, `modelo`, `promptVersion`, `entradaHash`, `desatualizada`).

**503:** IA desabilitada ou sem chave.

---

## 11. Health (fora de `/api/v1`)

| Rota | Função |
|---|---|
| `GET /health/live` | Processo vivo |
| `GET /health/ready` | Mongo + inicialização (sem Gemini) |

---

## 12. Exemplo completo — conversão

**Request**

```http
POST /api/v1/ideias/ide-550e8400-e29b-41d4-a716-446655440000/projeto
Authorization: Bearer …
Content-Type: application/json

{
  "versao": 4,
  "nome": "Automação de checklist",
  "descricao": "Projeto derivado da ideia aprovada",
  "responsavelId": "usr-gestor-001",
  "etapa": "Planejamento",
  "status": "PLANEJADO",
  "investimento": 1000,
  "retornoFinanceiro": 1500,
  "reducaoCustos": 0,
  "ganhoProdutividade": 12.5,
  "prazo": "2026-12-31"
}
```

**Response 201**

```json
{
  "id": "prj-7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "nome": "Automação de checklist",
  "descricao": "Projeto derivado da ideia aprovada",
  "ideiaId": "ide-550e8400-e29b-41d4-a716-446655440000",
  "estrategiaId": "est-001",
  "estrategiaVersao": 2,
  "responsavelId": "usr-gestor-001",
  "responsavelNome": "Gestor Demo",
  "etapa": "Planejamento",
  "status": "PLANEJADO",
  "investimento": 1000,
  "retornoFinanceiro": 1500,
  "reducaoCustos": 0,
  "ganhoProdutividade": 12.5,
  "prazo": "2026-12-31",
  "versao": 1,
  "excluidaEm": null,
  "criadoEm": "2026-09-20T14:35:00Z",
  "atualizadoEm": "2026-09-20T14:35:00Z"
}
```

Ideia correspondente passa a `status: VIROU_PROJETO` na mesma transação.

---

## 13. Enums (referência rápida)

| Enum | Valores |
|---|---|
| `Perfil` | OPERADOR, GESTOR, LIDER |
| `StatusIdeia` | ENVIADA, EM_ANALISE, APROVADA, REJEITADA, VIROU_PROJETO |
| `PrioridadeIdeia` | BAIXA, MEDIA, ALTA |
| `StatusProjeto` | PLANEJADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO |
| `PrioridadeSugeridaIa` | BAIXA, MEDIA, ALTA |

---

## 14. Validação do OpenAPI

Na etapa 2+, validar com:

```bash
npx --yes @redocly/cli lint docs/sprint2/openapi.yaml
```

Ou importar `openapi.yaml` no Swagger Editor.
