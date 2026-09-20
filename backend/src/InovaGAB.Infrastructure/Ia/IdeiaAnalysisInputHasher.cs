using System.Security.Cryptography;
using System.Text;
using System.Text.Json;

namespace InovaGAB.Infrastructure.Ia;

internal static class IdeiaAnalysisInputHasher
{
    public static string ComputeHash(IdeiaAnalysisInputSnapshot snapshot)
    {
        var json = JsonSerializer.Serialize(snapshot, new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase });
        var bytes = SHA256.HashData(Encoding.UTF8.GetBytes(json));
        return Convert.ToHexString(bytes).ToLowerInvariant();
    }
}

internal sealed class IdeiaAnalysisInputSnapshot
{
    public string PromptVersion { get; init; } = string.Empty;

    public string Titulo { get; init; } = string.Empty;

    public string Area { get; init; } = string.Empty;

    public string Descricao { get; init; } = string.Empty;

    public string EstrategiaId { get; init; } = string.Empty;

    public int EstrategiaVersao { get; init; }

    public string EstrategiaTitulo { get; init; } = string.Empty;

    public string EstrategiaDescricao { get; init; } = string.Empty;

    public string EstrategiaCategoria { get; init; } = string.Empty;
}
