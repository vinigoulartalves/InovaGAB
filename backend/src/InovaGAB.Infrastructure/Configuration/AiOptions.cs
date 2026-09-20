namespace InovaGAB.Infrastructure.Configuration;

public sealed class AiOptions
{
    public const string SectionName = "AI";

    public bool Enabled { get; set; }

    public string? ApiKey { get; set; }

    /// <summary>
    /// Modelo Gemini (configurável via AI__Model). Padrão: gemini-2.0-flash — ver docs/sprint2/IA_GEMINI.md.
    /// </summary>
    public string Model { get; set; } = "gemini-2.0-flash";

    public int TimeoutSeconds { get; set; } = 60;

    public int MaxOutputTokens { get; set; } = 2048;

    public int MaxTituloChars { get; set; } = 300;

    public int MaxAreaChars { get; set; } = 200;

    public int MaxDescricaoChars { get; set; } = 8000;

    public int MaxEstrategiaTituloChars { get; set; } = 300;

    public int MaxEstrategiaDescricaoChars { get; set; } = 4000;

    public Uri BaseUrl { get; set; } = new("https://generativelanguage.googleapis.com/");
}
