# Evidência — teste real Gemini (opt-in)

| Campo | Valor |
|---|---|
| Status | **PENDENTE** (agente Cloud sem `AI_API_KEY` / quota) |
| Modelo configurado | `gemini-2.0-flash` (`AI__Model`) |
| Data da consulta à doc | 2026-09-20 |
| Fonte | https://ai.google.dev/gemini-api/docs/structured-output |
| Teste | `tests/InovaGAB.IaExternalTests/GeminiRealAnalysisTests.cs` |

## Como registrar evidência após execução local

1. Exportar `AI_API_KEY` e `MONGODB_URI`.
2. Executar `dotnet test tests/InovaGAB.IaExternalTests`.
3. Atualizar esta tabela com: data/hora UTC, modelo retornado, HTTP status, campos validados (pontuação 0–100, prioridade, justificativa, riscos) **sem** expor a chave ou texto completo da ideia.

Exemplo sanitizado:

```
status=OK modelo=gemini-2.0-flash pontuacao=68 prioridade=MEDIA campos=notas,justificativa,riscos,melhorias
```
