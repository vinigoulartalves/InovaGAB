using InovaGAB.Application.Auth.Dtos;

namespace InovaGAB.Application.Auth;

public interface IAuthService
{
    Task<LoginResponseDto> LoginAsync(LoginRequestDto request, CancellationToken cancellationToken);

    Task<LoginResponseDto> RefreshAsync(RefreshRequestDto request, CancellationToken cancellationToken);

    Task LogoutAsync(RefreshRequestDto request, CancellationToken cancellationToken);

    Task<UsuarioResumoDto> GetMeAsync(string usuarioId, CancellationToken cancellationToken);

    Task<IReadOnlyList<ResponsavelResumoDto>> ListResponsaveisAsync(CancellationToken cancellationToken);
}
