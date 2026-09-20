using InovaGAB.Application.Exceptions;
using InovaGAB.Application.Ideias;

namespace InovaGAB.IntegrationTests.Ia;

internal sealed class FakeGeminiIdeiaAnalysisClient : IGeminiIdeiaAnalysisClient
{
    public GeminiIdeiaAnalysisMode Mode { get; set; } = GeminiIdeiaAnalysisMode.Success;

    public Task<GeminiIdeiaAnalysisResult> AnalyzeAsync(
        GeminiIdeiaAnalysisRequest request,
        CancellationToken cancellationToken)
    {
        return Mode switch
        {
            GeminiIdeiaAnalysisMode.Success => Task.FromResult(new GeminiIdeiaAnalysisResult
            {
                RawJson = """
                    {
                      "pontuacaoTotal": 72,
                      "alinhamentoEstrategico": 80,
                      "impacto": 70,
                      "viabilidade": 65,
                      "prioridadeSugerida": "MEDIA",
                      "justificativa": "A ideia está alinhada à estratégia e tem impacto moderado.",
                      "riscos": ["Dependência de capacitação da equipe"],
                      "melhorias": ["Detalhar métricas de sucesso"]
                    }
                    """
            }),
            GeminiIdeiaAnalysisMode.InvalidJson => Task.FromResult(new GeminiIdeiaAnalysisResult
            {
                RawJson = "{ \"pontuacaoTotal\": 150 }"
            }),
            GeminiIdeiaAnalysisMode.Quota => throw new IaDependencyException(429, "IA_LIMITE", "Limite excedido."),
            GeminiIdeiaAnalysisMode.ServerError => throw new IaDependencyException(502, "IA_INDISPONIVEL", "Indisponível."),
            _ => throw new InvalidOperationException("Modo de teste inválido.")
        };
    }
}

internal enum GeminiIdeiaAnalysisMode
{
    Success,
    InvalidJson,
    Quota,
    ServerError
}
