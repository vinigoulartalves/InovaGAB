# InovaGAB Sprint 2 — STATUS

**Atualizado em:** 2026-09-20 (UTC)  
**Etapa atual:** 1 — Diagnóstico local e contrato  
**Branch de trabalho:** `cursor/sprint2-docs-etapa1-befe`  
**Push remoto:** não executado (conforme instrução do usuário)

---

## 1. Comparação com a base `9665412ce08110cf2e1f11ded6e5adbb13e9ec9d`

| Aspecto | Base (9665412) | Checkout atual |
|---|---|---|
| Android Sprint 1 | Kotlin/Compose/Firebase | **Preservado** (sem alterações nesta etapa) |
| Documentação técnica Sprint 1 | `docs/DOCUMENTACAO_TECNICA_INOVAGAB.md` | **Preservada** |
| Sprint 2 guia | Ausente | `docs/sprint2/InovaGAB_Sprint2_Guia_e_Prompts_Cursor.md` (+ cópia `GUIA_CURSOR.md`) |
| Backend / Docker / testes | Ausente | **Ainda ausente** (etapa 2+) |
| Artefatos contrato etapa 1 | Ausente | `PLANO_EXECUCAO.md`, `CONTRATO_API.md`, `openapi.yaml`, `RASTREABILIDADE.md`, este `STATUS.md` |

**Diff resumido desde a base:** apenas adição do guia Sprint 2 no commit `b5acf7e`; nesta etapa acrescentamos documentação de contrato sem tocar no código Android.

---

## 2. O que está implementado / executado / pendente

| Item | Estado | Notas |
|---|---|---|
| Plano de execução | **Implementado** (doc) | `PLANO_EXECUCAO.md` |
| Contrato HTTP + OpenAPI | **Implementado** (doc) | Validar lint na etapa 2 |
| Rastreabilidade | **Implementado** (doc) | OBR vs PLUS |
| Backend .NET | **Pendente** | Prompt 2 |
| MongoDB / Compose | **Pendente** | Prompt 2 |
| Auth / CRUDs / IA | **Pendente** | Prompts 3–6 |
| Android REST | **Pendente** | Prompts 7–8 |
| CI / entrega ZIP | **Pendente** | Prompts 9–10 |
| Validação OpenAPI (`redocly lint`) | **Executado** | `npx @redocly/cli lint docs/sprint2/openapi.yaml` — 0 erros (avisos Redocly aceitáveis: localhost, tags sem description) |
| Testes automatizados | **Não executado** | Sem código backend ainda |

---

## 3. Decisões registradas (etapa 1)

1. **Monólito modular** Api / Application / Domain / Infrastructure.
2. **JWT + `PasswordHasher<TUser>`** sem Identity completo (`UserManager` / `IdentityDbContext` SQL).
3. **MongoDB** novo com seed demo; Firebase remoto intocado.
4. **EF Core 8.4.4 + Driver 3.11.2** — CRUD via EF; índices, agregações e transações multi-documento críticas via **mesma sessão** ou driver explícito.
5. **Concorrência:** campo `versao` em mutações; **`If-Match: W/"{versao}"`** em DELETE lógico.
6. **Exclusão lógica** com `excluidaEm`; listagens omitam; pontos de ideia excluída **mantidos**.
7. **Vigência** com data civil em `America/Sao_Paulo`; instantes em UTC.
8. **Ranking** derivado de eventos, não campo editável no usuário.
9. **IA Gemini** como Plus; core sobe sem IA no readiness.

---

## 4. Inconsistências e gaps identificados

| # | Descrição | Mitigação planejada |
|---|---|---|
| I-01 | Android `Orientacao` não tem categoria/campanha/vigência | Expandir UI/API na etapa 8; contrato já prevê campos |
| I-02 | Android usa `Long` para datas; API usa ISO UTC | Mappers Retrofit na etapa 7 |
| I-03 | `User.pontos` no Firestore vs eventos no servidor | Remover escrita local; ranking só via API |
| I-04 | `Projeto.ideiaId` vazio no form atual | Conversão via endpoint dedicado (etapa 5/8) |
| I-05 | Arquivo guia nomeado `InovaGAB_Sprint2_...` vs `GUIA_CURSOR.md` esperado | Criado `GUIA_CURSOR.md` cópia do guia |
| I-06 | **AGENTS.md** não existe no repositório | Seguir regras do `GUIA_CURSOR.md` §3; considerar adicionar AGENTS na raiz em etapa futura se desejado |

---

## 5. Dependências externas

| Dependência | Uso | Bloqueio se ausente |
|---|---|---|
| Docker + Compose | API, Mongo rs0, test-runner | Etapa 2 registra bloqueio, não sucesso falso |
| .NET 8 SDK | Build backend | Idem |
| Gemini API (chave, quota, rede) | Análise IA Plus | 503 + teste opt-in pendente |
| Android SDK / emulador | APK e jornadas | Lint/build local; instrumentado opcional |
| GitHub Actions runners | CI | YAML não implica aprovação remota |

---

## 6. Versões propostas (validar na etapa 2)

| Componente | Versão | Fonte |
|---|---|---|
| .NET SDK | 8.0.x LTS | https://dotnet.microsoft.com/download/dotnet/8.0 |
| `Microsoft.EntityFrameworkCore` | 8.0.30 | NuGet (req. `MongoDB.EntityFrameworkCore` 8.4.4) |
| `MongoDB.EntityFrameworkCore` | 8.4.4 | https://www.nuget.org/packages/MongoDB.EntityFrameworkCore/8.4.4 |
| `MongoDB.Driver` | 3.11.2 | Dependência do provider |
| MongoDB Server (dev) | 7.0 (digest fixado na etapa 2) | https://hub.docker.com/_/mongo |
| Limitações EF Mongo | Consultar doc oficial na versão adotada | https://www.mongodb.com/docs/entity-framework/current/limitations/ |

**Comando de verificação previsto (etapa 2):**

```bash
dotnet --version
docker compose version
npx --yes @redocly/cli lint docs/sprint2/openapi.yaml
```

---

## 7. Comandos úteis (ainda não criados no repo)

Conforme guia §15 — serão adicionados nos prompts 2 e 9:

- `bash scripts/setup-dev.sh`
- `docker compose up -d --build`
- `bash scripts/smoke-test.sh`
- `./gradlew assembleDebug`

---

## 8. Próxima etapa

**Prompt 2:** fundação backend (`backend/InovaGAB.sln`), Docker Compose com Mongo replica set, healthchecks, teste de integração EF↔Mongo, `.env.example`, scripts setup — **sem módulos de negócio**.

---

## 9. Evidências desta etapa

| Evidência | Local |
|---|---|
| Plano arquitetural | `docs/sprint2/PLANO_EXECUCAO.md` |
| Contrato narrativo | `docs/sprint2/CONTRATO_API.md` |
| OpenAPI 3.0 | `docs/sprint2/openapi.yaml` |
| Rastreabilidade | `docs/sprint2/RASTREABILIDADE.md` |
| Guia operacional | `docs/sprint2/GUIA_CURSOR.md` |

*Nenhum vídeo ou teste de runtime nesta etapa (escopo documental apenas).*
