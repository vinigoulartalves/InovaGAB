using System.Security.Cryptography;
using InovaGAB.Application.Auth;
using InovaGAB.Application.Auth.Dtos;
using InovaGAB.Application.Exceptions;
using InovaGAB.Domain.Auth;
using InovaGAB.Domain.Usuarios;
using InovaGAB.Infrastructure.Configuration;
using InovaGAB.Infrastructure.Persistence;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace InovaGAB.Infrastructure.Auth;

public sealed class AuthService : IAuthService
{
    private readonly InovaGabDbContext _dbContext;
    private readonly PasswordHasher<Usuario> _passwordHasher;
    private readonly JwtAccessTokenFactory _jwtFactory;
    private readonly JwtOptions _jwtOptions;
    private readonly TimeProvider _timeProvider;
    private readonly ILogger<AuthService> _logger;

    public AuthService(
        InovaGabDbContext dbContext,
        PasswordHasher<Usuario> passwordHasher,
        JwtAccessTokenFactory jwtFactory,
        IOptions<JwtOptions> jwtOptions,
        TimeProvider timeProvider,
        ILogger<AuthService> logger)
    {
        _dbContext = dbContext;
        _passwordHasher = passwordHasher;
        _jwtFactory = jwtFactory;
        _jwtOptions = jwtOptions.Value;
        _timeProvider = timeProvider;
        _logger = logger;
    }

    public async Task<LoginResponseDto> LoginAsync(LoginRequestDto request, CancellationToken cancellationToken)
    {
        var emailNormalizado = NormalizeEmail(request.Email);
        var usuario = await _dbContext.Usuarios
            .FirstOrDefaultAsync(u => u.EmailNormalizado == emailNormalizado, cancellationToken);

        if (usuario is null || !usuario.Ativo)
        {
            throw new AuthException(401, "CREDENCIAIS_INVALIDAS", AuthErrors.CredenciaisInvalidas);
        }

        var verify = _passwordHasher.VerifyHashedPassword(usuario, usuario.PasswordHash, request.Senha);
        if (verify == PasswordVerificationResult.Failed)
        {
            throw new AuthException(401, "CREDENCIAIS_INVALIDAS", AuthErrors.CredenciaisInvalidas);
        }

        _logger.LogInformation("Login bem-sucedido para usuário {UsuarioId}", usuario.Id);
        return await IssueTokensAsync(usuario, cancellationToken);
    }

    public async Task<LoginResponseDto> RefreshAsync(RefreshRequestDto request, CancellationToken cancellationToken)
    {
        var hash = RefreshTokenHasher.Hash(request.RefreshToken);
        var stored = await _dbContext.RefreshTokens
            .FirstOrDefaultAsync(t => t.TokenHash == hash, cancellationToken);

        if (stored is null)
        {
            throw new AuthException(401, "REFRESH_INVALIDO", AuthErrors.RefreshInvalido);
        }

        if (stored.RevokedAtUtc is not null)
        {
            throw new AuthException(409, "REFRESH_REUTILIZADO", AuthErrors.RefreshReutilizado);
        }

        var now = _timeProvider.GetUtcNow().UtcDateTime;
        if (stored.ExpiresAtUtc <= now)
        {
            throw new AuthException(401, "REFRESH_INVALIDO", AuthErrors.RefreshInvalido);
        }

        var usuario = await _dbContext.Usuarios
            .FirstOrDefaultAsync(u => u.Id == stored.UsuarioId && u.Ativo, cancellationToken);

        if (usuario is null)
        {
            throw new AuthException(401, "REFRESH_INVALIDO", AuthErrors.RefreshInvalido);
        }

        await using var transaction = await _dbContext.Database.BeginTransactionAsync(cancellationToken);

        stored.RevokedAtUtc = now;
        stored.Versao++;
        var newPlainRefresh = GenerateRefreshToken();
        var newEntity = CreateRefreshEntity(usuario.Id, newPlainRefresh, now);
        stored.ReplacedByTokenId = newEntity.Id;
        _dbContext.RefreshTokens.Add(newEntity);

        try
        {
            await _dbContext.SaveChangesAsync(cancellationToken);
        }
        catch (DbUpdateConcurrencyException)
        {
            throw new AuthException(409, "REFRESH_REUTILIZADO", AuthErrors.RefreshReutilizado);
        }

        var (accessToken, expiresAt) = _jwtFactory.Create(usuario);
        await transaction.CommitAsync(cancellationToken);

        return BuildLoginResponse(usuario, accessToken, expiresAt, newPlainRefresh);
    }

    public async Task LogoutAsync(RefreshRequestDto request, CancellationToken cancellationToken)
    {
        var hash = RefreshTokenHasher.Hash(request.RefreshToken);
        var stored = await _dbContext.RefreshTokens
            .FirstOrDefaultAsync(t => t.TokenHash == hash, cancellationToken);

        if (stored is null || stored.RevokedAtUtc is not null)
        {
            return;
        }

        stored.RevokedAtUtc = _timeProvider.GetUtcNow().UtcDateTime;
        stored.Versao++;
        await _dbContext.SaveChangesAsync(cancellationToken);
    }

    public async Task<UsuarioResumoDto> GetMeAsync(string usuarioId, CancellationToken cancellationToken)
    {
        var usuario = await _dbContext.Usuarios
            .AsNoTracking()
            .FirstOrDefaultAsync(u => u.Id == usuarioId && u.Ativo, cancellationToken);

        if (usuario is null)
        {
            throw new AuthException(401, "NAO_AUTENTICADO", "Sessão inválida.");
        }

        return MapUsuario(usuario);
    }

    public async Task<IReadOnlyList<ResponsavelResumoDto>> ListResponsaveisAsync(CancellationToken cancellationToken)
    {
        return await _dbContext.Usuarios
            .AsNoTracking()
            .Where(u => u.Ativo && u.Perfil == PerfilUsuario.GESTOR)
            .OrderBy(u => u.Nome)
            .Select(u => new ResponsavelResumoDto { Id = u.Id, Nome = u.Nome })
            .ToListAsync(cancellationToken);
    }

    private async Task<LoginResponseDto> IssueTokensAsync(Usuario usuario, CancellationToken cancellationToken)
    {
        var now = _timeProvider.GetUtcNow().UtcDateTime;
        var plainRefresh = GenerateRefreshToken();
        var refreshEntity = CreateRefreshEntity(usuario.Id, plainRefresh, now);
        _dbContext.RefreshTokens.Add(refreshEntity);
        await _dbContext.SaveChangesAsync(cancellationToken);

        var (accessToken, expiresAt) = _jwtFactory.Create(usuario);
        return BuildLoginResponse(usuario, accessToken, expiresAt, plainRefresh);
    }

    private RefreshToken CreateRefreshEntity(string usuarioId, string plainRefresh, DateTime now)
    {
        return new RefreshToken
        {
            Id = Guid.NewGuid().ToString("N"),
            UsuarioId = usuarioId,
            TokenHash = RefreshTokenHasher.Hash(plainRefresh),
            CreatedAtUtc = now,
            ExpiresAtUtc = now.AddDays(_jwtOptions.RefreshTokenDays),
            Versao = 1
        };
    }

    private static LoginResponseDto BuildLoginResponse(
        Usuario usuario,
        string accessToken,
        DateTime expiresAt,
        string plainRefresh)
    {
        return new LoginResponseDto
        {
            AccessToken = accessToken,
            ExpiresAt = expiresAt,
            RefreshToken = plainRefresh,
            Usuario = MapUsuario(usuario)
        };
    }

    private static UsuarioResumoDto MapUsuario(Usuario usuario) =>
        new()
        {
            Id = usuario.Id,
            Nome = usuario.Nome,
            Email = usuario.Email,
            Perfil = usuario.Perfil
        };

    private static string GenerateRefreshToken()
    {
        var bytes = RandomNumberGenerator.GetBytes(64);
        return Convert.ToBase64String(bytes);
    }

    public static string NormalizeEmail(string email) =>
        email.Trim().ToLowerInvariant();
}
