namespace InovaGAB.Domain.Estrategias;

public sealed class EstrategiaSnapshot
{
    public string Titulo { get; set; } = string.Empty;

    public string Descricao { get; set; } = string.Empty;

    public string Categoria { get; set; } = string.Empty;

    public string Campanha { get; set; } = string.Empty;

    public DateOnly InicioVigencia { get; set; }

    public DateOnly? FimVigencia { get; set; }

    public bool Ativa { get; set; }
}
