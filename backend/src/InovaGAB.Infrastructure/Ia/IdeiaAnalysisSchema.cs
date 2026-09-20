namespace InovaGAB.Infrastructure.Ia;

internal static class IdeiaAnalysisSchema
{
    public const string JsonSchema = """
        {
          "type": "object",
          "properties": {
            "pontuacaoTotal": { "type": "integer", "minimum": 0, "maximum": 100 },
            "alinhamentoEstrategico": { "type": "integer", "minimum": 0, "maximum": 100 },
            "impacto": { "type": "integer", "minimum": 0, "maximum": 100 },
            "viabilidade": { "type": "integer", "minimum": 0, "maximum": 100 },
            "prioridadeSugerida": { "type": "string", "enum": ["BAIXA", "MEDIA", "ALTA"] },
            "justificativa": { "type": "string", "maxLength": 2000 },
            "riscos": {
              "type": "array",
              "maxItems": 10,
              "items": { "type": "string", "maxLength": 500 }
            },
            "melhorias": {
              "type": "array",
              "maxItems": 10,
              "items": { "type": "string", "maxLength": 500 }
            }
          },
          "required": [
            "pontuacaoTotal",
            "alinhamentoEstrategico",
            "impacto",
            "viabilidade",
            "prioridadeSugerida",
            "justificativa",
            "riscos",
            "melhorias"
          ]
        }
        """;
}
