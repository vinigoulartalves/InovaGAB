namespace InovaGAB.Infrastructure.Configuration;

public sealed class AiOptions
{
    public const string SectionName = "AI";

    public bool Enabled { get; set; }

    public string? ApiKey { get; set; }

    public string Model { get; set; } = "gemini-2.0-flash";
}
