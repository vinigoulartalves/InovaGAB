namespace InovaGAB.Domain.Ideias;

public sealed class Ideia
{
    public string Id { get; set; } = string.Empty;

    public string Titulo { get; set; } = string.Empty;

    public string Descricao { get; set; } = string.Empty;

    public string Area { get; set; } = string.Empty;

    public string AutorId { get; set; } = string.Empty;

    public StatusIdeia Status { get; set; } = StatusIdeia.ENVIADA;

    public PrioridadeIdeia Prioridade { get; set; } = PrioridadeIdeia.MEDIA;

    public string EstrategiaId { get; set; } = string.Empty;

    public int EstrategiaVersao { get; set; }

    public int Versao { get; set; } = 1;

    public DateTime? ExcluidaEmUtc { get; set; }

    public DateTime CriadoEmUtc { get; set; }

    public DateTime AtualizadoEmUtc { get; set; }
}
