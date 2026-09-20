namespace InovaGAB.Application.Auth;

public static class AuthErrors
{
    public const string CredenciaisInvalidas = "Credenciais inválidas.";
    public const string RefreshInvalido = "Refresh token inválido ou expirado.";
    public const string RefreshReutilizado = "Refresh token já utilizado ou revogado.";
    public const string UsuarioInativo = "Usuário inativo.";
}
