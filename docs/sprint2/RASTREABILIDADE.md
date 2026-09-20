# InovaGAB Sprint 2 — Rastreabilidade

Liga **requisito** → **endpoint** → **tela Android (existente/planejada)** → **teste** → **evidência futura**.

Legenda de prioridade:

| Tag | Significado |
|---|---|
| **OBR** | Obrigatório funcional / integração Sprint 2 |
| **PLUS** | Diferencial (IA real, automação avançada, entrega PDF, etc.) |

Status de implementação nesta etapa: **documentado** (código e testes nas etapas 2–10).

---

## 1. Autenticação e sessão

| ID | Requisito | Endpoint | Tela / componente Android | Teste planejado | Evidência futura | Pri |
|---|---|---|---|---|---|---|
| R-AUTH-01 | Login email/senha | `POST /api/v1/auth/login` | `LoginViewModel`, `LoginScreen` | Integração HTTP login válido/inválido | Log teste + Postman | OBR |
| R-AUTH-02 | Refresh com rotação | `POST /api/v1/auth/refresh` | `SessionManager`, interceptor OkHttp | Reuso/concorrência refresh | TRX xUnit | OBR |
| R-AUTH-03 | Logout revoga refresh | `POST /api/v1/auth/logout` | Logout em app | Logout idempotente | Postman | OBR |
| R-AUTH-04 | Perfil autenticado | `GET /api/v1/auth/me` | Restauração sessão `InovaGabApp` | Token expirado / me 200 | Screenshot sessão | OBR |
| R-AUTH-05 | Hash senha Identity, sem expor | (interno) | — | JSON sem `passwordHash` | Assert resposta | OBR |
| R-AUTH-06 | Sem cadastro público | — | — | Tentativa registro 404/405 | Doc STATUS | OBR |

---

## 2. Estratégias (orientações)

| ID | Requisito | Endpoint | Tela Android | Teste | Evidência | Pri |
|---|---|---|---|---|---|---|
| R-EST-01 | Listar com filtros vigente/categoria | `GET /api/v1/estrategias` | Operador/Gestor/Líder consulta orientações | Filtro vigente | `EstrategiasIdeiasTests` + HTTP | OBR |
| R-EST-02 | CRUD líder | `POST/PUT/DELETE /api/v1/estrategias` | `OrientacaoFormScreen`, `LiderViewModel` | Role LIDER vs OPERADOR 403 | E2E líder | OBR |
| R-EST-03 | Histórico imutável | `GET /api/v1/estrategias/{id}/historico` | Tela histórico (prompt 8) | Edição gera entrada | Mongo snapshot | OBR |
| R-EST-04 | Vigência SP + datas civis | (regra) | Formulário datas | Estratégia vencida bloqueia ideia | Teste relógio injetável | OBR |
| R-EST-05 | Exclusão lógica + If-Match | `DELETE` + header | Confirmação exclusão | 409 versão errada | Postman | OBR |
| R-EST-06 | Categoria/campanha | campos DTO | UI líder (prompt 8) | Payload create | OpenAPI exemplo | OBR |

---

## 3. Ideias

| ID | Requisito | Endpoint | Tela Android | Teste | Evidência | Pri |
|---|---|---|---|---|---|---|
| R-IDE-01 | CRUD próprio operador | `GET/POST/PUT/DELETE /api/v1/ideias` | `IdeiaFormScreen`, `MinhasIdeiasScreen` | operador2 não acessa ideia1 | E2E isolamento | OBR |
| R-IDE-02 | Vínculo estratégia vigente | `POST /api/v1/ideias` | Form com seleção estratégia | 409 estratégia arquivada | HTTP | OBR |
| R-IDE-03 | Mass assignment negado | create/update DTO | — | Enviar `status`/`prioridade` 400 | xUnit | OBR |
| R-IDE-04 | Gestor lista todas | `GET /api/v1/ideias` | `GestaoIdeiasScreen` | Filtros status | Postman gestor | OBR |
| R-IDE-05 | Avaliação e transições | `PATCH .../avaliacao` | `GestorViewModel` | 409 transição ilegal | Matriz transições | OBR |
| R-IDE-06 | Concorrência `versao` | PUT/PATCH | Edição com conflito | 409 CONCORRENCIA | Dois clients | OBR |
| R-IDE-07 | Exclusão lógica ENVIADA | `DELETE` + If-Match | Minhas ideias | Pontos mantidos | Assert eventos | OBR |

---

## 4. Conversão e projetos

| ID | Requisito | Endpoint | Tela Android | Teste | Evidência | Pri |
|---|---|---|---|---|---|---|
| R-PRJ-01 | Conversão transacional | `POST /api/v1/ideias/{id}/projeto` | `ProjetoFormScreen` pré-preenchido | Uma conversão/ideia | E2E gestor | OBR |
| R-PRJ-02 | Estratégia vigente na conversão | (regra) | Bloqueio UI origem | 409 vigência | HTTP | OBR |
| R-PRJ-03 | CRUD gestor | `GET/POST/PUT/DELETE /api/v1/projetos` | `ProjetoFormScreen`, gestão | Líder read-only | Roles | OBR |
| R-PRJ-04 | Criação direta sem ideiaId | `POST /api/v1/projetos` | Novo projeto gestor | Rejeita ideiaId | xUnit | OBR |
| R-PRJ-05 | Responsáveis gestores ativos | `GET /api/v1/usuarios/responsaveis` | Combo responsável | Gestor inativo 400 | Seed | OBR |
| R-PRJ-06 | PUT não relinca estratégia | `PUT /api/v1/projetos/{id}` | Edição projeto | estrategiaId imutável | Unit | OBR |
| R-PRJ-07 | Exclusão não reabre ideia | DELETE lógico | — | Segunda conversão 409 | Mongo | OBR |

---

## 5. Pontuação e ranking

| ID | Requisito | Endpoint | Tela Android | Teste | Evidência | Pri |
|---|---|---|---|---|---|---|
| R-PT-01 | +10 cadastro atômico | (evento no POST ideia) | — | Índice único evento | Integração | OBR |
| R-PT-02 | +30 primeira aprovação | (evento no PATCH) | — | Reaprovação não duplica | xUnit | OBR |
| R-PT-03 | Ranking sem email | `GET /api/v1/ranking` | Ranking todas homes | Ordenação estável | Screenshot ranking | OBR |
| R-PT-04 | App não grava pontos | — | Remover write Firestore | Sem campo pontos no client | Code review | OBR |

---

## 6. Relatórios e dashboard

| ID | Requisito | Endpoint | Tela Android | Teste | Evidência | Pri |
|---|---|---|---|---|---|---|
| R-REL-01 | Dashboard líder | `GET /api/v1/relatorios/dashboard` | `DashboardScreen` | Cenário A+B lucro 1100 ROI ~36,67% | Assert números | OBR |
| R-REL-02 | ROI investimento zero null | (regra) | UI "Não aplicável" | Projeto investimento 0 | Unit relatório | OBR |
| R-REL-03 | Por estratégia | `GET /api/v1/relatorios/estrategias` | Filtros dashboard | Agregação driver | Log pipeline | OBR |
| R-REL-04 | Projeto individual | `GET /api/v1/relatorios/projetos/{id}` | Detalhe | Atrasado prazo | HTTP | OBR |
| R-REL-05 | Gráficos barras/status | séries no dashboard | Compose Canvas (prompt 8) | Séries não vazias seed | Vídeo demo | OBR |
| R-REL-06 | Excluídos fora do total | (regra) | — | DELETE projeto altera totais | Integração | OBR |

---

## 7. IA (Plus)

| ID | Requisito | Endpoint | Tela Android | Teste | Evidência | Pri |
|---|---|---|---|---|---|---|
| R-IA-01 | Análise real Gemini | `POST /api/v1/ideias/{id}/analises-ia` | Botão gestão (prompt 8) | Opt-in chave real | Log sanitizado | PLUS |
| R-IA-02 | Histórico análises | `GET .../analises-ia` | Lista análises | Paginação | Postman | PLUS |
| R-IA-03 | Não altera status/prioridade | (regra) | Aplicar via avaliação | POST IA não muda ideia | Assert DB | PLUS |
| R-IA-04 | Indisponível sem chave | 503 ProblemDetails | Mensagem erro | Sem JSON fake | Teste Test env | PLUS |
| R-IA-05 | Desatualizada após edição | flag `desatualizada` | Badge UI | PUT ideia após IA | Integração | PLUS |

---

## 8. Infraestrutura e qualidade

| ID | Requisito | Endpoint | Artefato | Teste | Evidência | Pri |
|---|---|---|---|---|---|---|
| R-INF-01 | Health live/ready | `/health/live`, `/health/ready` | docker-compose | smoke curl | CI log | OBR |
| R-INF-02 | EF Core CRUD Mongo real | — | Etapa 2 integração | Grava/lê documento | STATUS etapa 2 | OBR |
| R-INF-03 | Transações replica set | — | rs0 compose | Conversão rollback | xUnit | OBR |
| R-INF-04 | OpenAPI = implementação | `openapi.yaml` | Swagger export | diff contrato | PR etapa 10 | OBR |
| R-INF-05 | Postman collection | — | `deliverables/` | Runner perfis | Artefato ZIP | OBR |
| R-INF-06 | CI GitHub Actions | — | `.github/workflows` | Build backend+Android | Badge (não afirmar sem run) | OBR |
| R-INF-07 | APK debug entrega | — | `assembleDebug` | Instalação dispositivo | APK no ZIP | OBR |
| R-INF-08 | Apresentação PDF/PPT | — | `APRESENTACAO.md` | Export pendente | Arquivo entrega | PLUS |

---

## 9. Versões a validar na etapa 2

| Pacote / runtime | Versão proposta | Validação |
|---|---|---|
| .NET SDK | 8.0.x | `dotnet --version` |
| `Microsoft.EntityFrameworkCore` | 8.0.30 | restore + teste integração |
| `MongoDB.EntityFrameworkCore` | 8.4.4 | CRUD real + concorrência |
| `MongoDB.Driver` | 3.11.2 | índices + aggregation relatórios |
| MongoDB Server | 7.0 (imagem fixada) | transação em rs0 |
| `Microsoft.Extensions.Identity.Core` | 8.0.x | PasswordHasher round-trip |

**Riscos documentados (não assumir suporte total):**

- LINQ `GroupBy`/joins complexos → driver aggregation.
- Identity completo com stores Mongo → fora do escopo; só PasswordHasher.
- Duas sessões Mongo (EF SaveChanges + InsertOne separados) → **proibido** para operações que exigem atomicidade.

---

## 10. Mapa rápido endpoint → arquivo de teste (futuro)

| Área | Caminho de teste sugerido |
|---|---|
| Auth | `tests/InovaGAB.IntegrationTests/Auth/` |
| Estratégias/Ideias | `tests/InovaGAB.IntegrationTests/Ideias/` |
| Projetos/Conversão | `tests/InovaGAB.IntegrationTests/Projetos/` |
| Relatórios | `tests/InovaGAB.IntegrationTests/Relatorios/` |
| IA opt-in | `tests/InovaGAB.IaExternalTests/` |
| Android mapeamento | `app/src/test/.../api/` |
| E2E HTTP | `scripts/smoke-test.sh`, Postman |
