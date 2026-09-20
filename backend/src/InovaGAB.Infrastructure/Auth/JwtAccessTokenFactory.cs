using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using InovaGAB.Domain.Usuarios;
using InovaGAB.Infrastructure.Configuration;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;

namespace InovaGAB.Infrastructure.Auth;

public sealed class JwtAccessTokenFactory
{
    private readonly JwtOptions _options;
    private readonly TimeProvider _timeProvider;

    public JwtAccessTokenFactory(IOptions<JwtOptions> options, TimeProvider timeProvider)
    {
        _options = options.Value;
        _timeProvider = timeProvider;
    }

    public (string Token, DateTime ExpiresAtUtc) Create(Usuario usuario)
    {
        var now = _timeProvider.GetUtcNow().UtcDateTime;
        var expires = now.AddMinutes(_options.AccessTokenMinutes);
        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_options.Secret));
        var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

        var claims = new List<Claim>
        {
            new(JwtRegisteredClaimNames.Sub, usuario.Id),
            new(ClaimTypes.Role, usuario.Perfil.ToString()),
            new(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString("N"))
        };

        var token = new JwtSecurityToken(
            issuer: _options.Issuer,
            audience: _options.Audience,
            claims: claims,
            notBefore: now,
            expires: expires,
            signingCredentials: credentials);

        var handler = new JwtSecurityTokenHandler();
        return (handler.WriteToken(token), expires);
    }
}
