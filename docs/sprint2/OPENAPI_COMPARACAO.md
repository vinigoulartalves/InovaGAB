# OpenAPI — contrato vs implementação

| Artefato | Caminho |
|----------|---------|
| Contrato versionado (etapa 1+) | `docs/sprint2/openapi.yaml` |
| Export runtime (opcional, gerado) | `docs/sprint2/openapi-runtime.json` (gitignored) |
| Swagger UI | `http://127.0.0.1:8080/swagger` (Development) |
| JSON Swagger | `GET /swagger/v1/swagger.json` |

## Comparação estática (paths)

Rotas registradas nos controllers + `Program.cs` vs paths em `openapi.yaml`:

| Path | openapi.yaml | Código |
|------|--------------|--------|
| `/api/v1/auth/*` (4) | Sim | `AuthController` |
| `/api/v1/usuarios/responsaveis` | Sim | `UsuariosController` |
| `/api/v1/estrategias` (+ id, historico) | Sim | `EstrategiasController` |
| `/api/v1/ideias` (+ avaliacao, projeto, analises-ia) | Sim | `IdeiasController` |
| `/api/v1/projetos` | Sim | `ProjetosController` |
| `/api/v1/relatorios/*` (3) | Sim | `RelatoriosController` |
| `/api/v1/ranking` | Sim | `RankingController` |
| `/health/live`, `/health/ready` | Sim | `Program.cs` |

**Resultado (2026-09-20):** conjuntos de paths **alinhados**; nenhuma rota extra de negócio no código sem entrada no YAML.

Diferenças esperadas no export runtime vs YAML manual:

- Descrições e exemplos podem variar (SwaggerGen reflete DTOs C#).
- Enums e schemas devem permanecer camelCase (config JSON na API).

## Exportar OpenAPI da API real

**Bash (após `bash scripts/dev-up.sh`):**

```bash
bash scripts/export-openapi.sh
```

**PowerShell:**

```powershell
.\scripts\export-openapi.ps1
```

O script grava `docs/sprint2/openapi-runtime.json` e imprime diff resumido de paths (`jq` se disponível).

### Execução neste ambiente

| Ambiente | Export runtime |
|----------|----------------|
| Agente Cloud (sem Docker) | **Não executado** — API não subiu |
| Máquina local com Compose | Executar scripts acima |

Após export local, revisar:

```bash
diff -u <(yq '.paths | keys' docs/sprint2/openapi.yaml) <(jq -r '.paths | keys[]' docs/sprint2/openapi-runtime.json | sort)
```

(Substituir por comparação manual de lista de paths se `yq`/`jq` ausentes.)
