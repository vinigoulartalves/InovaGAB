namespace InovaGAB.Domain.Estrategias;

public sealed class EstrategiaHistorico
{
    public string Id { get; set; } = string.Empty;

    public string EstrategiaId { get; set; } = string.Empty;

    public int Versao { get; set; }

    public EstrategiaHistoricoAcao Acao { get; set; }

    public string AtorId { get; set; } = string.Empty;

    public DateTime OcorridoEmUtc { get; set; }

    public EstrategiaSnapshot Snapshot { get; set; } = new();
}
