using InovaGAB.Domain.Ideias;

namespace InovaGAB.Application.Ideias.Dtos;

public sealed class IdeiaListQuery
{
    public StatusIdeia? Status { get; set; }

    public PrioridadeIdeia? Prioridade { get; set; }

    public string? EstrategiaId { get; set; }

    public string? AutorId { get; set; }

    public int Page { get; set; } = 1;

    public int PageSize { get; set; } = 20;

    public string? Sort { get; set; }
}

public class IdeiaResumoDto
{
    public string Id { get; set; } = string.Empty;

    public string Titulo { get; set; } = string.Empty;

    public string Area { get; set; } = string.Empty;

    public StatusIdeia Status { get; set; }

    public PrioridadeIdeia Prioridade { get; set; }

    public string AutorNome { get; set; } = string.Empty;

    public string EstrategiaId { get; set; } = string.Empty;

    public DateTime CriadoEm { get; set; }
}

public sealed class IdeiaDetalheDto : IdeiaResumoDto
{
    public string Descricao { get; set; } = string.Empty;

    public string AutorId { get; set; } = string.Empty;

    public int EstrategiaVersao { get; set; }

    public int Versao { get; set; }

    public DateTime? ExcluidaEm { get; set; }

    public DateTime AtualizadoEm { get; set; }
}

public sealed class IdeiaCreateRequestDto
{
    public string Titulo { get; set; } = string.Empty;

    public string Descricao { get; set; } = string.Empty;

    public string Area { get; set; } = string.Empty;

    public string EstrategiaId { get; set; } = string.Empty;
}

public sealed class IdeiaUpdateRequestDto
{
    public int Versao { get; set; }

    public string Titulo { get; set; } = string.Empty;

    public string Descricao { get; set; } = string.Empty;

    public string Area { get; set; } = string.Empty;
}

public sealed class IdeiaAvaliacaoRequestDto
{
    public int Versao { get; set; }

    public StatusIdeia Status { get; set; }

    public PrioridadeIdeia? Prioridade { get; set; }

    public string? Justificativa { get; set; }
}
