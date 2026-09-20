namespace InovaGAB.Application.Ideias;

public interface IGeminiIdeiaAnalysisClient
{
    Task<GeminiIdeiaAnalysisResult> AnalyzeAsync(
        GeminiIdeiaAnalysisRequest request,
        CancellationToken cancellationToken);
}

public sealed class GeminiIdeiaAnalysisRequest
{
    public string Model { get; init; } = string.Empty;

    public string SystemInstruction { get; init; } = string.Empty;

    public string UserPrompt { get; init; } = string.Empty;

    public string ResponseJsonSchema { get; init; } = string.Empty;

    public int MaxOutputTokens { get; init; }
}

public sealed class GeminiIdeiaAnalysisResult
{
    public string RawJson { get; init; } = string.Empty;
}
