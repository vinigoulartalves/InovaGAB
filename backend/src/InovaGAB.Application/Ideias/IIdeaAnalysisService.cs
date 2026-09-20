using InovaGAB.Application.Common;
using InovaGAB.Application.Ideias.Dtos;

namespace InovaGAB.Application.Ideias;

public interface IIdeaAnalysisService
{
    Task<AnaliseIaDetalheDto> SolicitarAnaliseAsync(string ideiaId, CancellationToken cancellationToken);

    Task<PagedResultDto<AnaliseIaResumoDto>> ListarHistoricoAsync(
        string ideiaId,
        int page,
        int pageSize,
        CancellationToken cancellationToken);
}
