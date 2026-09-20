# InovaGAB Sprint 2 — STATUS

**Atualizado em:** 2026-09-20 (UTC)  
**Etapa atual:** 3 — Autenticação e autorização  
**Branch:** `cursor/sprint2-backend-foundation-befe` (continuação)

---

## 1. Resumo etapa 3

| Item | Estado |
|---|---|
| `POST /api/v1/auth/login` | **Implementado** (+ rate limit IP) |
| `POST /api/v1/auth/refresh` | **Implementado** (rotação, 409 reutilização) |
| `POST /api/v1/auth/logout` | **Implementado** (idempotente) |
| `GET /api/v1/auth/me` | **Implementado** |
| `GET /api/v1/usuarios/responsaveis` | **Implementado** (policy GESTOR) |
| Seed demo idempotente | **Implementado** (`DevDataSeeder`, senhas via `.env`) |
| Índice único `emailNormalizado` | **Implementado** (driver) |
| Testes `WebApplicationFactory` + Mongo | **Implementados** — execução requer `MONGODB_URI` |
| CRUDs negócio (ideias, etc.) | **Pendente** (404 — teste registra pendência) |

---

## 2. Segurança documentada

- **JWT access** válido até `exp`; **não há revogação instantânea** do access token — apenas expiração natural.
- **Logout** revoga o **refresh** (hash no MongoDB).
- Login falho: mensagem genérica *Credenciais inválidas* (401).
- Sem cadastro público; role **não** aceita do cliente no login.

---

## 3. Contas demo (seed)

Criadas **uma vez** se não existirem (`Demo = true`), senhas em:

- `DEV_PASSWORD_OPERADOR1` → `operador1@inovagab.local`
- `DEV_PASSWORD_OPERADOR2` → `operador2@inovagab.local`
- `DEV_PASSWORD_GESTOR` → `gestor@inovagab.local`
- `DEV_PASSWORD_LIDER` → `lider@inovagab.local`

Geradas por `scripts/setup-dev.sh` — **não** commitar `.env`.

---

## 4. Testes executados (agente Cloud)

```bash
export PATH="$HOME/.dotnet:$PATH"
cd backend && dotnet build -c Release && dotnet test -c Release
```

| Resultado | Motivo |
|---|---|
| Build **OK** | — |
| Testes auth/EF **falharam** | **MongoDB/Docker indisponível** no pod (`MONGODB_URI` ausente) |

Com Docker:

```bash
docker compose up -d --build
docker compose --profile tests run --rm test-runner
```

---

## 5. Próxima etapa

**Prompt 4:** estratégias e ideias (CRUD, histórico, pontuação).

---

## 6. Referências

- [`CONTRATO_API.md`](./CONTRATO_API.md) §3 Autenticação
- [`backend/README.md`](../../backend/README.md) § Autenticação
