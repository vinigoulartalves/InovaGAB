using System.IdentityModel.Tokens.Jwt;
using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Security.Claims;
using System.Text;
using System.Text.Json;
using InovaGAB.Application.Auth.Dtos;
using InovaGAB.Domain.Auth;
using InovaGAB.Infrastructure.Auth;
using InovaGAB.Infrastructure.Persistence;
using InovaGAB.IntegrationTests.Infrastructure;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.IdentityModel.Tokens;
using Xunit;

namespace InovaGAB.IntegrationTests.Auth;

public sealed class AuthEndpointsTests
{
    private InovaGabWebApplicationFactory? _factory;
    private HttpClient? _client;

    private HttpClient Client
    {
        get
        {
            EnsureMongo();
            _factory ??= new InovaGabWebApplicationFactory
            {
                MongoConnectionString = MongoTestEnvironment.ConnectionString
            };
            return _client ??= _factory.CreateClient();
        }
    }

    [Fact]
    public async Task Login_valido_retorna_tokens_sem_hash()
    {
        EnsureMongo();
        await AuthTestSeed.SeedAsync(_factory!.Services);

        var response = await Client.PostAsJsonAsync("/api/v1/auth/login", new LoginRequestDto
        {
            Email = AuthTestSeed.OperadorEmail,
            Senha = AuthTestSeed.TestPassword
        });

        response.EnsureSuccessStatusCode();
        var json = await response.Content.ReadAsStringAsync();
        Assert.DoesNotContain("passwordHash", json, StringComparison.OrdinalIgnoreCase);
        Assert.DoesNotContain("PasswordHash", json);

        var body = JsonSerializer.Deserialize<LoginResponseDto>(
            json,
            new JsonSerializerOptions { PropertyNameCaseInsensitive = true });

        Assert.NotNull(body);
        Assert.False(string.IsNullOrWhiteSpace(body!.AccessToken));
        Assert.False(string.IsNullOrWhiteSpace(body.RefreshToken));
        Assert.Equal(AuthTestSeed.OperadorEmail, body.Usuario.Email);
    }

    [Fact]
    public async Task Login_invalido_retorna_401_generico()
    {
        EnsureMongo();
        await AuthTestSeed.SeedAsync(_factory!.Services);

        var response = await Client.PostAsJsonAsync("/api/v1/auth/login", new LoginRequestDto
        {
            Email = AuthTestSeed.OperadorEmail,
            Senha = "senha-errada-xyz"
        });

        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
        var json = await response.Content.ReadAsStringAsync();
        Assert.Contains("Credenciais inválidas", json);
    }

    [Fact]
    public async Task Token_adulterado_retorna_401_no_me()
    {
        EnsureMongo();
        await AuthTestSeed.SeedAsync(_factory!.Services);
        var login = await LoginAsync(AuthTestSeed.OperadorEmail);

        var tampered = login.AccessToken[..^1] + (login.AccessToken[^1] == 'a' ? 'b' : 'a');
        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", tampered);

        var me = await Client.GetAsync("/api/v1/auth/me");
        Assert.Equal(HttpStatusCode.Unauthorized, me.StatusCode);
    }

    [Fact]
    public async Task Token_expirado_retorna_401_no_me()
    {
        EnsureMongo();
        await AuthTestSeed.SeedAsync(_factory!.Services);

        var token = CreateExpiredToken();
        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        var me = await Client.GetAsync("/api/v1/auth/me");
        Assert.Equal(HttpStatusCode.Unauthorized, me.StatusCode);
    }

    [Fact]
    public async Task Refresh_valido_rotaciona_e_reuso_retorna_409()
    {
        EnsureMongo();
        await AuthTestSeed.SeedAsync(_factory!.Services);
        var login = await LoginAsync(AuthTestSeed.OperadorEmail);
        var oldRefresh = login.RefreshToken;

        var refreshResponse = await Client.PostAsJsonAsync("/api/v1/auth/refresh", new RefreshRequestDto
        {
            RefreshToken = oldRefresh
        });
        refreshResponse.EnsureSuccessStatusCode();

        var reuse = await Client.PostAsJsonAsync("/api/v1/auth/refresh", new RefreshRequestDto
        {
            RefreshToken = oldRefresh
        });
        Assert.Equal(HttpStatusCode.Conflict, reuse.StatusCode);
    }

    [Fact]
    public async Task Refresh_expirado_retorna_401()
    {
        EnsureMongo();
        await AuthTestSeed.SeedAsync(_factory!.Services);
        var login = await LoginAsync(AuthTestSeed.OperadorEmail);

        await using var scope = _factory!.Services.CreateAsyncScope();
        var db = scope.ServiceProvider.GetRequiredService<InovaGabDbContext>();
        var hash = RefreshTokenHasher.Hash(login.RefreshToken);
        var stored = await db.RefreshTokens.FirstAsync(t => t.TokenHash == hash);
        stored.ExpiresAtUtc = DateTime.UtcNow.AddMinutes(-5);
        await db.SaveChangesAsync();

        var response = await Client.PostAsJsonAsync("/api/v1/auth/refresh", new RefreshRequestDto
        {
            RefreshToken = login.RefreshToken
        });
        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
    }

    [Fact]
    public async Task Logout_revoga_refresh_e_e_idempotente()
    {
        EnsureMongo();
        await AuthTestSeed.SeedAsync(_factory!.Services);
        var login = await LoginAsync(AuthTestSeed.OperadorEmail);

        var logout = await Client.PostAsJsonAsync("/api/v1/auth/logout", new RefreshRequestDto
        {
            RefreshToken = login.RefreshToken
        });
        Assert.Equal(HttpStatusCode.NoContent, logout.StatusCode);

        var logoutAgain = await Client.PostAsJsonAsync("/api/v1/auth/logout", new RefreshRequestDto
        {
            RefreshToken = login.RefreshToken
        });
        Assert.Equal(HttpStatusCode.NoContent, logoutAgain.StatusCode);

        var refresh = await Client.PostAsJsonAsync("/api/v1/auth/refresh", new RefreshRequestDto
        {
            RefreshToken = login.RefreshToken
        });
        Assert.True(refresh.StatusCode is HttpStatusCode.Unauthorized or HttpStatusCode.Conflict);
    }

    [Fact]
    public async Task Responsaveis_apenas_gestor()
    {
        EnsureMongo();
        await AuthTestSeed.SeedAsync(_factory!.Services);

        var operadorLogin = await LoginAsync(AuthTestSeed.OperadorEmail);
        Client.DefaultRequestHeaders.Authorization =
            new AuthenticationHeaderValue("Bearer", operadorLogin.AccessToken);
        var forbidden = await Client.GetAsync("/api/v1/usuarios/responsaveis");
        Assert.Equal(HttpStatusCode.Forbidden, forbidden.StatusCode);

        var gestorLogin = await LoginAsync(AuthTestSeed.GestorEmail);
        Client.DefaultRequestHeaders.Authorization =
            new AuthenticationHeaderValue("Bearer", gestorLogin.AccessToken);
        var ok = await Client.GetAsync("/api/v1/usuarios/responsaveis");
        ok.EnsureSuccessStatusCode();
    }

    [Fact]
    public async Task Operador_nao_acessa_projetos()
    {
        EnsureMongo();
        await AuthTestSeed.SeedAsync(_factory!.Services);
        var login = await LoginAsync(AuthTestSeed.OperadorEmail);
        Client.DefaultRequestHeaders.Authorization =
            new AuthenticationHeaderValue("Bearer", login.AccessToken);

        var projetos = await Client.GetAsync("/api/v1/projetos");
        Assert.Equal(HttpStatusCode.Forbidden, projetos.StatusCode);
    }

    private async Task<LoginResponseDto> LoginAsync(string email)
    {
        var response = await Client.PostAsJsonAsync("/api/v1/auth/login", new LoginRequestDto
        {
            Email = email,
            Senha = AuthTestSeed.TestPassword
        });
        response.EnsureSuccessStatusCode();
        return (await response.Content.ReadFromJsonAsync<LoginResponseDto>())!;
    }

    private string CreateExpiredToken()
    {
        var key = new SymmetricSecurityKey(
            Encoding.UTF8.GetBytes("integration-test-secret-min-32-characters-long!"));
        var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);
        var token = new JwtSecurityToken(
            issuer: "inovagab-test",
            audience: "inovagab-test",
            claims: new[] { new Claim(JwtRegisteredClaimNames.Sub, "expired-user") },
            expires: DateTime.UtcNow.AddMinutes(-10),
            signingCredentials: credentials);
        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    private static void EnsureMongo()
    {
        if (!MongoTestEnvironment.IsAvailable)
        {
            throw new InvalidOperationException(
                "MongoDB indisponível para testes de autenticação. Defina MONGODB_URI.");
        }
    }
}
