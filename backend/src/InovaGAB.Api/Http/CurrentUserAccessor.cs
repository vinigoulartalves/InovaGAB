using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using InovaGAB.Domain.Usuarios;

namespace InovaGAB.Api.Http;

public static class CurrentUserAccessor
{
    public static string GetUserId(ClaimsPrincipal user)
    {
        return user.FindFirstValue(JwtRegisteredClaimNames.Sub)
            ?? user.FindFirstValue(ClaimTypes.NameIdentifier)
            ?? throw new InvalidOperationException("Usuário não autenticado.");
    }

    public static PerfilUsuario GetPerfil(ClaimsPrincipal user)
    {
        var role = user.FindFirstValue(ClaimTypes.Role)
            ?? user.FindFirstValue("role");

        if (string.IsNullOrWhiteSpace(role) || !Enum.TryParse<PerfilUsuario>(role, out var perfil))
        {
            throw new InvalidOperationException("Perfil inválido no token.");
        }

        return perfil;
    }
}
