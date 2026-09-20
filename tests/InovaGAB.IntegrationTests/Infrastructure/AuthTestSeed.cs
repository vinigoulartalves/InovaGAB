using InovaGAB.Domain.Usuarios;
using InovaGAB.Infrastructure.Auth;
using InovaGAB.Infrastructure.Persistence;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;

namespace InovaGAB.IntegrationTests.Infrastructure;

internal static class AuthTestSeed
{
    public const string OperadorEmail = "operador-test@inovagab.local";
    public const string GestorEmail = "gestor-test@inovagab.local";
    public const string TestPassword = "TestPassword123!";

    public static async Task SeedAsync(IServiceProvider services)
    {
        await using var scope = services.CreateAsyncScope();
        var db = scope.ServiceProvider.GetRequiredService<InovaGabDbContext>();
        var hasher = scope.ServiceProvider.GetRequiredService<PasswordHasher<Usuario>>();

        await db.Database.EnsureCreatedAsync();

        if (!await db.Usuarios.AnyAsync(u => u.EmailNormalizado == AuthService.NormalizeEmail(OperadorEmail)))
        {
            var operador = CreateUser("Operador Teste", OperadorEmail, PerfilUsuario.OPERADOR, hasher);
            db.Usuarios.Add(operador);
        }

        if (!await db.Usuarios.AnyAsync(u => u.EmailNormalizado == AuthService.NormalizeEmail(GestorEmail)))
        {
            var gestor = CreateUser("Gestor Teste", GestorEmail, PerfilUsuario.GESTOR, hasher);
            db.Usuarios.Add(gestor);
        }

        await db.SaveChangesAsync();
    }

    private static Usuario CreateUser(string nome, string email, PerfilUsuario perfil, PasswordHasher<Usuario> hasher)
    {
        var usuario = new Usuario
        {
            Id = Guid.NewGuid().ToString("N"),
            Nome = nome,
            Email = email,
            EmailNormalizado = AuthService.NormalizeEmail(email),
            Perfil = perfil,
            Ativo = true,
            Demo = true,
            CriadoEmUtc = DateTime.UtcNow
        };
        usuario.PasswordHash = hasher.HashPassword(usuario, TestPassword);
        return usuario;
    }
}
