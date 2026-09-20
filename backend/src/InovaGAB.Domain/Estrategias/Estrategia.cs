namespace InovaGAB.Domain.Estrategias;

public sealed class Estrategia
{
    public string Id { get; set; } = string.Empty;

    public string Titulo { get; set; } = string.Empty;

    public string Descricao { get; set; } = string.Empty;

    public string Categoria { get; set; } = string.Empty;

    public string Campanha { get; set; } = string.Empty;

    public DateOnly InicioVigencia { get; set; }

    public DateOnly? FimVigencia { get; set; }

    public bool Ativa { get; set; } = true;

    public int Versao { get; set; } = 1;

    public DateTime CriadoEmUtc { get; set; }

    public DateTime AtualizadoEmUtc { get; set; }

    public DateTime? ExcluidaEmUtc { get; set; }
}
