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
bash scripts/setup-dev.sh          # cria .env (não sobrescreve existente)
docker compose up -d --build       # mongo rs0 + api em http://127.0.0.1:8080
bash scripts/smoke-test.sh         # /health/live e /health/ready
docker compose --profile tests run --rm test-runner
```

**Host sem Compose:** defina `MONGODB_URI` (replica set ou `directConnection=true` para nó único em localhost) e variáveis `Mongo__*`, `Jwt__Secret`; execute a API a partir de `backend/`.

Documentação detalhada: [`backend/README.md`](backend/README.md) e [`docs/sprint2/STATUS.md`](docs/sprint2/STATUS.md).
