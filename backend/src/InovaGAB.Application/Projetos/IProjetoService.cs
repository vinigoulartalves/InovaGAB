using InovaGAB.Application.Common;
using InovaGAB.Application.Projetos.Dtos;
using InovaGAB.Domain.Usuarios;

namespace InovaGAB.Application.Projetos;

public interface IProjetoService
{
    Task<PagedResultDto<ProjetoResumoDto>> ListAsync(
        ProjetoListQuery query,
        PerfilUsuario perfil,
        CancellationToken cancellationToken);

    Task<ProjetoDetalheDto> GetAsync(string id, PerfilUsuario perfil, CancellationToken cancellationToken);

    Task<ProjetoDetalheDto> CreateAsync(ProjetoCreateRequestDto request, CancellationToken cancellationToken);

    Task<ProjetoDetalheDto> UpdateAsync(string id, ProjetoUpdateRequestDto request, CancellationToken cancellationToken);

    Task DeleteLogicalAsync(string id, int versaoEsperada, CancellationToken cancellationToken);

    Task<ProjetoDetalheDto> ConverterIdeiaAsync(
        string ideiaId,
        ConversaoIdeiaProjetoRequestDto request,
        CancellationToken cancellationToken);
}
