using System.Net.Http.Headers;
using System.Net.Http.Json;
using InovaGAB.Application.Auth.Dtos;
using InovaGAB.Application.Estrategias.Dtos;
using InovaGAB.Application.Ideias.Dtos;
using InovaGAB.Infrastructure.Persistence;
using InovaGAB.IntegrationTests.Infrastructure;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Xunit;
using Xunit.Abstractions;

namespace InovaGAB.IaExternalTests;

/// <summary>
/// Opt-in: requer MONGODB_URI e AI_API_KEY (ou AI__ApiKey) com quota válida no Google AI Studio.
/// </summary>
public sealed class GeminiRealAnalysisTests
{
    private readonly ITestOutputHelper _output;

    public GeminiRealAnalysisTests(ITestOutputHelper output) => _output = output;

    [Fact]
    public async Task Chamada_real_Gemini_persiste_analise()
    {
        var apiKey = Environment.GetEnvironmentVariable("AI_API_KEY")
            ?? Environment.GetEnvironmentVariable("AI__ApiKey")
            ?? Environment.GetEnvironmentVariable("GEMINI_API_KEY");

        Assert.False(
            string.IsNullOrWhiteSpace(apiKey),
            "PENDENTE/opt-in: defina AI_API_KEY antes de executar InovaGAB.IaExternalTests (não é sucesso silencioso).");

        Assert.True(
            MongoTestEnvironment.IsAvailable,
            "PENDENTE: MONGODB_URI ausente para teste real Gemini.");

        await using var factory = new InovaGabWebApplicationFactory
        {
            MongoConnectionString = MongoTestEnvironment.ConnectionString
        };
        factory.UseSetting("AI:Enabled", "true");
        factory.UseSetting("AI:ApiKey", apiKey);
        factory.UseSetting("AI:Model", Environment.GetEnvironmentVariable("AI_MODEL") ?? "gemini-2.0-flash");

        await AuthTestSeed.SeedAsync(factory.Services);
        var client = factory.CreateClient();

        var lider = await LoginAsync(client, AuthTestSeed.LiderEmail);
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", lider.AccessToken);
        var estResp = await client.PostAsJsonAsync("/api/v1/estrategias", new EstrategiaCreateRequestDto
        {
            Titulo = "Estratégia real IA",
            Descricao = "Foco em eficiência operacional.",
            Categoria = "Operações",
            Campanha = "REAL-IA",
            InicioVigencia = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(-1)),
            FimVigencia = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(90)),
            Ativa = true
        });
        estResp.EnsureSuccessStatusCode();
        var estrategia = (await estResp.Content.ReadFromJsonAsync<EstrategiaDetalheDto>())!;

        var operador = await LoginAsync(client, AuthTestSeed.OperadorEmail);
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", operador.AccessToken);
        var ideiaResp = await client.PostAsJsonAsync("/api/v1/ideias", new IdeiaCreateRequestDto
        {
            Titulo = "Automação de relatórios",
            Descricao = "Reduzir trabalho manual com integração entre sistemas.",
            Area = "TI",
            EstrategiaId = estrategia.Id
        });
        ideiaResp.EnsureSuccessStatusCode();
        var ideia = (await ideiaResp.Content.ReadFromJsonAsync<IdeiaDetalheDto>())!;

        var gestor = await LoginAsync(client, AuthTestSeed.GestorEmail);
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", gestor.AccessToken);
        var analiseResp = await client.PostAsync($"/api/v1/ideias/{ideia.Id}/analises-ia", null);
        analiseResp.EnsureSuccessStatusCode();
        var analise = (await analiseResp.Content.ReadFromJsonAsync<AnaliseIaDetalheDto>())!;

        Assert.InRange(analise.PontuacaoTotal, 0, 100);
        Assert.False(string.IsNullOrWhiteSpace(analise.Justificativa));
        Assert.NotEmpty(analise.Riscos);

        await using var scope = factory.Services.CreateAsyncScope();
        var db = scope.ServiceProvider.GetRequiredService<InovaGabDbContext>();
        Assert.True(await db.IdeiasAnalisesIa.AnyAsync(a => a.Id == analise.Id));

        _output.WriteLine($"EVIDENCIA modelo={analise.Modelo} status=OK pontuacao={analise.PontuacaoTotal} prioridade={analise.PrioridadeSugerida}");
    }

    private static async Task<LoginResponseDto> LoginAsync(HttpClient client, string email)
    {
        var response = await client.PostAsJsonAsync("/api/v1/auth/login", new LoginRequestDto
        {
            Email = email,
            Senha = AuthTestSeed.TestPassword
        });
        response.EnsureSuccessStatusCode();
        return (await response.Content.ReadFromJsonAsync<LoginResponseDto>())!;
    }
}
