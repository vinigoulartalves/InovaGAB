using System.Text.Json;
using InovaGAB.Application.Exceptions;
using InovaGAB.Domain.Ideias;

namespace InovaGAB.Infrastructure.Ia;

internal static class IdeiaAnalysisResponseParser
{
    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        PropertyNameCaseInsensitive = true
    };

    public static ParsedIdeiaAnalysis Parse(string rawJson)
    {
        try
        {
            var dto = JsonSerializer.Deserialize<GeminiIdeiaAnalysisPayload>(rawJson, JsonOptions);
            if (dto is null)
            {
                throw new IaDependencyException(502, "IA_RESPOSTA_INVALIDA", "A IA retornou JSON vazio ou inválido.");
            }

            ValidateScore(dto.PontuacaoTotal, nameof(dto.PontuacaoTotal));
            ValidateScore(dto.AlinhamentoEstrategico, nameof(dto.AlinhamentoEstrategico));
            ValidateScore(dto.Impacto, nameof(dto.Impacto));
            ValidateScore(dto.Viabilidade, nameof(dto.Viabilidade));

            if (!Enum.TryParse<PrioridadeSugeridaIa>(dto.PrioridadeSugerida, true, out var prioridade)
                || !Enum.IsDefined(prioridade))
            {
                throw new IaDependencyException(502, "IA_RESPOSTA_INVALIDA", "prioridadeSugerida inválida na resposta da IA.");
            }

            var justificativa = (dto.Justificativa ?? string.Empty).Trim();
            if (justificativa.Length == 0 || justificativa.Length > 2000)
            {
                throw new IaDependencyException(502, "IA_RESPOSTA_INVALIDA", "justificativa fora do tamanho permitido.");
            }

            var riscos = ValidateStringList(dto.Riscos, "riscos");
            var melhorias = ValidateStringList(dto.Melhorias, "melhorias");

            return new ParsedIdeiaAnalysis
            {
                PontuacaoTotal = dto.PontuacaoTotal,
                AlinhamentoEstrategico = dto.AlinhamentoEstrategico,
                Impacto = dto.Impacto,
                Viabilidade = dto.Viabilidade,
                PrioridadeSugerida = prioridade,
                Justificativa = justificativa,
                Riscos = riscos,
                Melhorias = melhorias
            };
        }
        catch (JsonException ex)
        {
            throw new IaDependencyException(502, "IA_RESPOSTA_INVALIDA", "Não foi possível interpretar o JSON da IA.", ex);
        }
    }

    private static void ValidateScore(int value, string field)
    {
        if (value < 0 || value > 100)
        {
            throw new IaDependencyException(502, "IA_RESPOSTA_INVALIDA", $"{field} deve estar entre 0 e 100.");
        }
    }

    private static List<string> ValidateStringList(IReadOnlyList<string>? items, string field)
    {
        if (items is null || items.Count == 0 || items.Count > 10)
        {
            throw new IaDependencyException(502, "IA_RESPOSTA_INVALIDA", $"{field} deve conter entre 1 e 10 itens.");
        }

        var list = new List<string>();
        foreach (var item in items)
        {
            var trimmed = (item ?? string.Empty).Trim();
            if (trimmed.Length == 0 || trimmed.Length > 500)
            {
                throw new IaDependencyException(502, "IA_RESPOSTA_INVALIDA", $"Item de {field} fora do tamanho permitido.");
            }

            list.Add(trimmed);
        }

        return list;
    }

    private sealed class GeminiIdeiaAnalysisPayload
    {
        public int PontuacaoTotal { get; set; }

        public int AlinhamentoEstrategico { get; set; }

        public int Impacto { get; set; }

        public int Viabilidade { get; set; }

        public string PrioridadeSugerida { get; set; } = string.Empty;

        public string? Justificativa { get; set; }

        public List<string>? Riscos { get; set; }

        public List<string>? Melhorias { get; set; }
    }
}

internal sealed class ParsedIdeiaAnalysis
{
    public int PontuacaoTotal { get; init; }

    public int AlinhamentoEstrategico { get; init; }

    public int Impacto { get; init; }

    public int Viabilidade { get; init; }

    public PrioridadeSugeridaIa PrioridadeSugerida { get; init; }

    public string Justificativa { get; init; } = string.Empty;

    public List<string> Riscos { get; init; } = new();

    public List<string> Melhorias { get; init; } = new();
}
