# InovaGAB

Plataforma mobile de inovação corporativa (FIAP + Grupo Águia Branca). Sprint 1: Android Kotlin/Compose com Firebase. Sprint 2: API .NET 8 + MongoDB + integração REST (em andamento).

## Estrutura

| Pasta | Conteúdo |
|---|---|
| `app/` | Android Sprint 1 (preservado) |
| `backend/` | API .NET 8 (Sprint 2) |
| `docs/sprint2/` | Contrato, plano, STATUS |
| `infra/` | Scripts MongoDB (replica set) |
| `scripts/` | Setup local, smoke tests |
| `tests/` | Testes de integração backend |

## Backend — início rápido (avaliação local)

> O `docker-compose.yml` é **somente para desenvolvimento/avaliação**, não para produção.

Pré-requisitos: Docker Compose, .NET SDK 8.0.425 (ver `backend/global.json`).

```bash
bash scripts/setup-dev.sh
bash scripts/dev-up.sh             # mongo rs0 + api + wait readiness
bash scripts/smoke-api.sh          # health + login seed
bash scripts/test-backend.sh       # integração (test-runner no compose)
bash scripts/test-android.sh     # unit + lint + APK
```

Guia completo: [`docs/sprint2/COMO_TESTAR.md`](docs/sprint2/COMO_TESTAR.md) · Matriz: [`docs/sprint2/MATRIZ_TESTES.md`](docs/sprint2/MATRIZ_TESTES.md)

**Host sem Compose:** defina `MONGODB_URI` (replica set ou `directConnection=true` para nó único em localhost) e variáveis `Mongo__*`, `Jwt__Secret`; execute a API a partir de `backend/`.

Documentação detalhada: [`backend/README.md`](backend/README.md) e [`docs/sprint2/STATUS.md`](docs/sprint2/STATUS.md).
