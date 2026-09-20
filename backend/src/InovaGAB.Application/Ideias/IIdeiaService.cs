using InovaGAB.Application.Common;
using InovaGAB.Application.Ideias.Dtos;
using InovaGAB.Domain.Usuarios;

namespace InovaGAB.Application.Ideias;

public interface IIdeiaService
{
    Task<PagedResultDto<IdeiaResumoDto>> ListAsync(
        IdeiaListQuery query,
        string usuarioId,
        PerfilUsuario perfil,
        CancellationToken cancellationToken);

    Task<IdeiaDetalheDto> GetAsync(string id, string usuarioId, PerfilUsuario perfil, CancellationToken cancellationToken);

    Task<IdeiaDetalheDto> CreateAsync(
        IdeiaCreateRequestDto request,
        string autorId,
        CancellationToken cancellationToken);

    Task<IdeiaDetalheDto> UpdateAsync(
        string id,
        IdeiaUpdateRequestDto request,
        string autorId,
        CancellationToken cancellationToken);

    Task DeleteLogicalAsync(string id, int versaoEsperada, string autorId, CancellationToken cancellationToken);

    Task<IdeiaDetalheDto> AvaliarAsync(
        string id,
        IdeiaAvaliacaoRequestDto request,
        string gestorId,
        CancellationToken cancellationToken);
}
