using InovaGAB.Domain.Ideias;

namespace InovaGAB.Application.Ideias.Dtos;

public sealed class AnaliseIaDetalheDto
{
    public string Id { get; set; } = string.Empty;

    public string IdeiaId { get; set; } = string.Empty;

    public int PontuacaoTotal { get; set; }

    public int AlinhamentoEstrategico { get; set; }

    public int Impacto { get; set; }

    public int Viabilidade { get; set; }

    public PrioridadeSugeridaIa PrioridadeSugerida { get; set; }

    public string Justificativa { get; set; } = string.Empty;

    public IReadOnlyList<string> Riscos { get; set; } = Array.Empty<string>();

    public IReadOnlyList<string> Melhorias { get; set; } = Array.Empty<string>();

    public string Provedor { get; set; } = string.Empty;

    public string Modelo { get; set; } = string.Empty;

    public string PromptVersion { get; set; } = string.Empty;

    public string EntradaHash { get; set; } = string.Empty;

    public bool Desatualizada { get; set; }

    public DateTime CriadoEm { get; set; }
}

public sealed class AnaliseIaResumoDto
{
    public string Id { get; set; } = string.Empty;

    public int PontuacaoTotal { get; set; }

    public PrioridadeSugeridaIa PrioridadeSugerida { get; set; }

    public bool Desatualizada { get; set; }

    public DateTime CriadoEm { get; set; }
}
