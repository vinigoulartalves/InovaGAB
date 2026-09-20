using InovaGAB.Application.Relatorios.Dtos;

namespace InovaGAB.Application.Relatorios;

public interface IRelatorioService
{
    Task<DashboardRelatorioDto> GetDashboardAsync(DashboardFiltroDto filtro, CancellationToken cancellationToken);

    Task<IReadOnlyList<RelatorioEstrategiaItemDto>> GetPorEstrategiaAsync(
        DashboardFiltroDto filtro,
        CancellationToken cancellationToken);

    Task<RelatorioProjetoDetalheDto> GetProjetoAsync(string projetoId, CancellationToken cancellationToken);
}
