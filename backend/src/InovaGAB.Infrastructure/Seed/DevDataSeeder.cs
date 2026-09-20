using InovaGAB.Domain.Usuarios;
using InovaGAB.Infrastructure.Auth;
using InovaGAB.Infrastructure.Configuration;
using InovaGAB.Infrastructure.Persistence;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace InovaGAB.Infrastructure.Seed;

public sealed class DevDataSeeder
{
    private readonly InovaGabDbContext _dbContext;
    private readonly PasswordHasher<Usuario> _passwordHasher;
    private readonly IConfiguration _configuration;
    private readonly SeedOptions _seedOptions;
    private readonly ILogger<DevDataSeeder> _logger;

    public DevDataSeeder(
        InovaGabDbContext dbContext,
        PasswordHasher<Usuario> passwordHasher,
        IConfiguration configuration,
        IOptions<SeedOptions> seedOptions,
        ILogger<DevDataSeeder> logger)
    {
        _dbContext = dbContext;
        _passwordHasher = passwordHasher;
        _configuration = configuration;
        _seedOptions = seedOptions.Value;
        _logger = logger;
    }

    public async Task SeedAsync(CancellationToken cancellationToken)
    {
        if (!_seedOptions.Enabled)
        {
            return;
        }

        var definitions = new[]
        {
            ("operador1@inovagab.local", "Operador Um", PerfilUsuario.OPERADOR, "DEV_PASSWORD_OPERADOR1"),
            ("operador2@inovagab.local", "Operador Dois", PerfilUsuario.OPERADOR, "DEV_PASSWORD_OPERADOR2"),
            ("gestor@inovagab.local", "Gestor Demo", PerfilUsuario.GESTOR, "DEV_PASSWORD_GESTOR"),
            ("lider@inovagab.local", "Líder Demo", PerfilUsuario.LIDER, "DEV_PASSWORD_LIDER")
        };

        foreach (var (email, nome, perfil, passwordKey) in definitions)
        {
            var normalizado = AuthService.NormalizeEmail(email);
            var exists = await _dbContext.Usuarios.AnyAsync(
                u => u.EmailNormalizado == normalizado,
                cancellationToken);

            if (exists)
            {
                continue;
            }

            var senha = _configuration[passwordKey];
            if (string.IsNullOrWhiteSpace(senha))
            {
                _logger.LogWarning(
                    "Seed ignorado para {Email}: variável {PasswordKey} não definida.",
                    email,
                    passwordKey);
                continue;
            }

            var usuario = new Usuario
            {
                Id = Guid.NewGuid().ToString("N"),
                Nome = nome,
                Email = email,
                EmailNormalizado = normalizado,
                Perfil = perfil,
                Ativo = true,
                Demo = true,
                CriadoEmUtc = DateTime.UtcNow
            };
            usuario.PasswordHash = _passwordHasher.HashPassword(usuario, senha);
            _dbContext.Usuarios.Add(usuario);
            _logger.LogInformation("Usuário demo criado: {Email} ({Perfil})", email, perfil);
        }

        await _dbContext.SaveChangesAsync(cancellationToken);
    }
}
