namespace InovaGAB.Domain.Usuarios;

public sealed class Usuario
{
    public string Id { get; set; } = string.Empty;

    public string Nome { get; set; } = string.Empty;

    public string Email { get; set; } = string.Empty;

    public string EmailNormalizado { get; set; } = string.Empty;

    public string PasswordHash { get; set; } = string.Empty;

    public PerfilUsuario Perfil { get; set; }

    public bool Ativo { get; set; } = true;

    public bool Demo { get; set; }

    public DateTime CriadoEmUtc { get; set; }
}
