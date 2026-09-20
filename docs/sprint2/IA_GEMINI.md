# InovaGAB — Integração Gemini (análise de ideias)

**Consulta à documentação:** 2026-09-20 (UTC)  
**Fontes oficiais:**

- Saída estruturada (JSON Schema): https://ai.google.dev/gemini-api/docs/structured-output  
- API `generateContent`: https://ai.google.dev/api/generate-content  
- Preços/limites (sujeitos à conta): https://ai.google.dev/pricing  

## Modelo padrão

| Campo | Valor |
|---|---|
| Identificador | `gemini-2.0-flash` |
| Configuração | `AI__Model` / `AI_MODEL` |
| Endpoint REST | `POST https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent` |
| Cliente | `IHttpClientFactory` (`GeminiIdeiaAnalysisClient`) — **sem SDK** |
| Saída | `generationConfig.responseMimeType = application/json` + `responseSchema` |

O modelo foi escolhido por combinar **saída estruturada** e **faixa gratuita com cotas** no Google AI Studio para contas elegíveis. **Não há gratuidade ilimitada**; o uso consome quota da conta e pode exigir faturamento conforme política Google. **Não há fallback automático** para modelos pagos.

## Variáveis de ambiente

| Variável | Seção .NET | Descrição |
|---|---|---|
| `AI_ENABLED` | `AI:Enabled` | `true` para habilitar análises |
| `AI_API_KEY` | `AI:ApiKey` | Chave do Google AI Studio (somente backend) |
| `AI_MODEL` | `AI:Model` | Modelo Gemini (padrão `gemini-2.0-flash`) |

Opcionais: `AI:TimeoutSeconds`, `AI:MaxOutputTokens`, limites de caracteres em `AiOptions`.

## Comandos locais

```bash
# .env (gerado por scripts/setup-dev.sh)
export AI_ENABLED=true
export AI_API_KEY="sua-chave-google-ai-studio"
export AI_MODEL=gemini-2.0-flash

cd backend && dotnet run --project src/InovaGAB.Api
```

Solicitar análise (gestor):

```bash
curl -s -X POST "http://localhost:8080/api/v1/ideias/{ideiaId}/analises-ia" \
  -H "Authorization: Bearer $GESTOR_TOKEN"
```

Sem chave ou com `AI_ENABLED=false`, a API retorna **503** `IA_INDISPONIVEL` — o core (auth, ideias, projetos, relatórios) continua operando.

## Testes

- Integração (HTTP mockado, ambiente de teste): `tests/InovaGAB.IntegrationTests/Ia/IdeiaAnalysisTests.cs`
- Chamada real opt-in:

```bash
export MONGODB_URI="mongodb://..."
export AI_API_KEY="..."
dotnet test tests/InovaGAB.IaExternalTests/InovaGAB.IaExternalTests.csproj
```

Evidência de execução real: `docs/sprint2/IA_EVIDENCIA.md`.
