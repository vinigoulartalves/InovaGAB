using InovaGAB.Domain.Usuarios;

namespace InovaGAB.Application.Auth.Dtos;

public sealed class UsuarioResumoDto
{
    public string Id { get; set; } = string.Empty;

    public string Nome { get; set; } = string.Empty;

    public string Email { get; set; } = string.Empty;

    public PerfilUsuario Perfil { get; set; }
}
