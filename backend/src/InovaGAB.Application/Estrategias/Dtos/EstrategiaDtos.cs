namespace InovaGAB.Application.Estrategias.Dtos;

public sealed class EstrategiaListQuery
{
    public bool? Vigente { get; set; }

    public string? Categoria { get; set; }

    public string? Campanha { get; set; }

    public int Page { get; set; } = 1;

    public int PageSize { get; set; } = 20;

    public string? Sort { get; set; }
}

public class EstrategiaResumoDto
{
    public string Id { get; set; } = string.Empty;

    public string Titulo { get; set; } = string.Empty;

    public string Categoria { get; set; } = string.Empty;

    public string Campanha { get; set; } = string.Empty;

    public DateOnly InicioVigencia { get; set; }

    public DateOnly? FimVigencia { get; set; }

    public bool Ativa { get; set; }

    public bool Vigente { get; set; }

    public int Versao { get; set; }
}

public sealed class EstrategiaDetalheDto : EstrategiaResumoDto
{
    public string Descricao { get; set; } = string.Empty;

    public DateTime? ExcluidaEm { get; set; }

    public DateTime CriadoEm { get; set; }

    public DateTime AtualizadoEm { get; set; }
}

public class EstrategiaCreateRequestDto
{
    public string Titulo { get; set; } = string.Empty;

    public string Descricao { get; set; } = string.Empty;

    public string Categoria { get; set; } = string.Empty;

    public string Campanha { get; set; } = string.Empty;

    public DateOnly InicioVigencia { get; set; }

    public DateOnly? FimVigencia { get; set; }

    public bool Ativa { get; set; } = true;
}

public sealed class EstrategiaUpdateRequestDto : EstrategiaCreateRequestDto
{
    public int Versao { get; set; }
}

public sealed class EstrategiaHistoricoItemDto
{
    public string Id { get; set; } = string.Empty;

    public int Versao { get; set; }

    public string Acao { get; set; } = string.Empty;

    public string AtorId { get; set; } = string.Empty;

    public DateTime OcorridoEm { get; set; }

    public EstrategiaSnapshotDto Snapshot { get; set; } = new();
}

public sealed class EstrategiaSnapshotDto
{
    public string Titulo { get; set; } = string.Empty;

    public string Descricao { get; set; } = string.Empty;

    public string Categoria { get; set; } = string.Empty;

    public string Campanha { get; set; } = string.Empty;

    public DateOnly InicioVigencia { get; set; }

    public DateOnly? FimVigencia { get; set; }

    public bool Ativa { get; set; }
}
