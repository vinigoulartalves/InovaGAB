namespace InovaGAB.Domain.Ideias;

public sealed class IdeiaAnaliseIa
{
    public string Id { get; set; } = string.Empty;

    public string IdeiaId { get; set; } = string.Empty;

    public int IdeiaVersaoEntrada { get; set; }

    public int PontuacaoTotal { get; set; }

    public int AlinhamentoEstrategico { get; set; }

    public int Impacto { get; set; }

    public int Viabilidade { get; set; }

    public PrioridadeSugeridaIa PrioridadeSugerida { get; set; }

    public string Justificativa { get; set; } = string.Empty;

    public List<string> Riscos { get; set; } = new();

    public List<string> Melhorias { get; set; } = new();

    public string Provedor { get; set; } = string.Empty;

    public string Modelo { get; set; } = string.Empty;

    public string PromptVersion { get; set; } = string.Empty;

    public string EntradaHash { get; set; } = string.Empty;

    public bool Desatualizada { get; set; }

    public DateTime CriadoEmUtc { get; set; }
}
