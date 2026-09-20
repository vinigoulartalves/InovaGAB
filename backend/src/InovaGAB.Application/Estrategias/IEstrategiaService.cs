using InovaGAB.Application.Common;
using InovaGAB.Application.Estrategias.Dtos;

namespace InovaGAB.Application.Estrategias;

public interface IEstrategiaService
{
    Task<PagedResultDto<EstrategiaResumoDto>> ListAsync(EstrategiaListQuery query, CancellationToken cancellationToken);

    Task<EstrategiaDetalheDto> GetAsync(string id, CancellationToken cancellationToken);

    Task<PagedResultDto<EstrategiaHistoricoItemDto>> GetHistoricoAsync(
        string id,
        int page,
        int pageSize,
        CancellationToken cancellationToken);

    Task<EstrategiaDetalheDto> CreateAsync(
        EstrategiaCreateRequestDto request,
        string atorId,
        CancellationToken cancellationToken);

    Task<EstrategiaDetalheDto> UpdateAsync(
        string id,
        EstrategiaUpdateRequestDto request,
        string atorId,
        CancellationToken cancellationToken);

    Task DeleteLogicalAsync(string id, int versaoEsperada, string atorId, CancellationToken cancellationToken);
}
