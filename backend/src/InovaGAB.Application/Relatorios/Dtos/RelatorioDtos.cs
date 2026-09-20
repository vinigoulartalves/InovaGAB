using InovaGAB.Application.Projetos.Dtos;
using InovaGAB.Domain.Projetos;

namespace InovaGAB.Application.Relatorios.Dtos;

public sealed class DashboardRelatorioDto
{
    public decimal InvestimentoTotal { get; set; }

    public decimal RetornoTotal { get; set; }

    public decimal LucroTotal { get; set; }

    public decimal? RoiPercentual { get; set; }

    public decimal ReducaoCustosTotal { get; set; }

    public decimal? GanhoProdutividadeMedio { get; set; }

    public int ProjetosAtrasados { get; set; }

    public IReadOnlyList<SerieInvestimentoRetornoDto> InvestimentoRetornoPorEstrategia { get; set; } =
        Array.Empty<SerieInvestimentoRetornoDto>();

    public IReadOnlyList<DistribuicaoStatusDto> DistribuicaoPorStatus { get; set; } =
        Array.Empty<DistribuicaoStatusDto>();
}

public sealed class SerieInvestimentoRetornoDto
{
    public string EstrategiaId { get; set; } = string.Empty;

    public string EstrategiaTitulo { get; set; } = string.Empty;

    public decimal Investimento { get; set; }

    public decimal Retorno { get; set; }
}

public sealed class DistribuicaoStatusDto
{
    public StatusProjeto Status { get; set; }

    public int Quantidade { get; set; }
}

public sealed class RelatorioEstrategiaItemDto
{
    public string EstrategiaId { get; set; } = string.Empty;

    public string Titulo { get; set; } = string.Empty;

    public decimal Investimento { get; set; }

    public decimal Retorno { get; set; }

    public decimal Lucro { get; set; }

    public decimal? RoiPercentual { get; set; }

    public int QuantidadeProjetos { get; set; }
}

public sealed class RelatorioProjetoDetalheDto
{
    public ProjetoDetalheDto Projeto { get; set; } = new();

    public decimal Lucro { get; set; }

    public decimal? RoiPercentual { get; set; }

    public bool Atrasado { get; set; }
}

public sealed class DashboardFiltroDto
{
    public string? EstrategiaId { get; set; }

    public string? ProjetoId { get; set; }

    public DateOnly? Inicio { get; set; }

    public DateOnly? Fim { get; set; }
}
