namespace InovaGAB.Application.Auth.Dtos;

public sealed class LoginResponseDto
{
    public string AccessToken { get; set; } = string.Empty;

    public DateTime ExpiresAt { get; set; }

    public string RefreshToken { get; set; } = string.Empty;

    public UsuarioResumoDto Usuario { get; set; } = new();
}
