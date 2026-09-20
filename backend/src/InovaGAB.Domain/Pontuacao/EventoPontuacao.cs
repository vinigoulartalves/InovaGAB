namespace InovaGAB.Domain.Pontuacao;

public sealed class EventoPontuacao
{
    public string Id { get; set; } = string.Empty;

    public string AutorId { get; set; } = string.Empty;

    public string IdeiaId { get; set; } = string.Empty;

    public TipoEventoPontuacao Tipo { get; set; }

    public int Pontos { get; set; }

    public DateTime OcorridoEmUtc { get; set; }
}
