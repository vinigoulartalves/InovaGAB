namespace InovaGAB.Domain.Auth;

public sealed class RefreshToken
{
    public string Id { get; set; } = string.Empty;

    public string UsuarioId { get; set; } = string.Empty;

    public string TokenHash { get; set; } = string.Empty;

    public DateTime ExpiresAtUtc { get; set; }

    public DateTime CreatedAtUtc { get; set; }

    public DateTime? RevokedAtUtc { get; set; }

    public string? ReplacedByTokenId { get; set; }

    public int Versao { get; set; }
}
