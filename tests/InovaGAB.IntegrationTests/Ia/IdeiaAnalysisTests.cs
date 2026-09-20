using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using InovaGAB.Application.Auth.Dtos;
using InovaGAB.Application.Common;
using InovaGAB.Application.Estrategias.Dtos;
using InovaGAB.Application.Ideias.Dtos;
using InovaGAB.Application.Ideias;
using InovaGAB.Domain.Ideias;
using InovaGAB.Infrastructure.Persistence;
using InovaGAB.IntegrationTests.Infrastructure;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Xunit;

namespace InovaGAB.IntegrationTests.Ia;

public sealed class IdeiaAnalysisTests
{
    private InovaGabWebApplicationFactory? _factory;
    private HttpClient? _client;
    private FakeGeminiIdeiaAnalysisClient? _fakeGemini;

    private InovaGabWebApplicationFactory Factory
    {
        get
        {
            _ = Client;
            return _factory!;
        }
    }

    private HttpClient Client
    {
        get
        {
            EnsureMongo();
            if (_factory is null)
            {
                _fakeGemini = new FakeGeminiIdeiaAnalysisClient();
                _factory = new InovaGabWebApplicationFactory
                {
                    MongoConnectionString = MongoTestEnvironment.ConnectionString,
                    ConfigureTestServices = services =>
                    {
                        var descriptor = services.SingleOrDefault(d => d.ServiceType == typeof(IGeminiIdeiaAnalysisClient));
                        if (descriptor is not null)
                        {
                            services.Remove(descriptor);
                        }

                        services.AddSingleton(_fakeGemini!);
                        services.AddSingleton<IGeminiIdeiaAnalysisClient>(sp => sp.GetRequiredService<FakeGeminiIdeiaAnalysisClient>());
                    }
                };
                _factory.UseSetting("AI:Enabled", "true");
                _factory.UseSetting("AI:ApiKey", "test-key-not-sent-to-network");
            }

            return _client ??= _factory.CreateClient();
        }
    }

    [Fact]
    public async Task Gestor_persiste_analise_e_operador_nao_acessa()
    {
        await AuthTestSeed.SeedAsync(Factory.Services);
        _fakeGemini!.Mode = GeminiIdeiaAnalysisMode.Success;

        var lider = await LoginAsync(AuthTestSeed.LiderEmail);
        var gestor = await LoginAsync(AuthTestSeed.GestorEmail);
        var operador = await LoginAsync(AuthTestSeed.OperadorEmail);

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", lider.AccessToken);
        var est = await Client.PostAsJsonAsync("/api/v1/estrategias", new EstrategiaCreateRequestDto
        {
            Titulo = "Estratégia IA",
            Descricao = "Desc estratégia",
            Categoria = "Cat",
            Campanha = "IA",
            InicioVigencia = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(-1)),
            FimVigencia = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(30)),
            Ativa = true
        });
        est.EnsureSuccessStatusCode();
        var estrategia = (await est.Content.ReadFromJsonAsync<EstrategiaDetalheDto>(IntegrationTestJson.Options))!;

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", operador.AccessToken);
        var ideiaResp = await Client.PostAsJsonAsync("/api/v1/ideias", new IdeiaCreateRequestDto
        {
            Titulo = "Ideia para IA",
            Descricao = "Descrição da ideia",
            Area = "Ops",
            EstrategiaId = estrategia.Id
        });
        ideiaResp.EnsureSuccessStatusCode();
        var ideia = (await ideiaResp.Content.ReadFromJsonAsync<IdeiaDetalheDto>(IntegrationTestJson.Options))!;

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", operador.AccessToken);
        Assert.Equal(HttpStatusCode.Forbidden, (await Client.PostAsync($"/api/v1/ideias/{ideia.Id}/analises-ia", null)).StatusCode);

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", gestor.AccessToken);
        var analiseResp = await Client.PostAsync($"/api/v1/ideias/{ideia.Id}/analises-ia", null);
        analiseResp.EnsureSuccessStatusCode();
        Assert.Equal(HttpStatusCode.Created, analiseResp.StatusCode);
        var analise = (await analiseResp.Content.ReadFromJsonAsync<AnaliseIaDetalheDto>(IntegrationTestJson.Options))!;
        Assert.Equal(72, analise.PontuacaoTotal);
        Assert.Equal("google-gemini", analise.Provedor);
        Assert.False(string.IsNullOrWhiteSpace(analise.EntradaHash));

        await using (var scope = Factory.Services.CreateAsyncScope())
        {
            var db = scope.ServiceProvider.GetRequiredService<InovaGabDbContext>();
            var stored = await db.IdeiasAnalisesIa.FirstAsync(a => a.Id == analise.Id);
            var ideiaDb = await db.Ideias.AsNoTracking().FirstAsync(i => i.Id == ideia.Id);
            Assert.Equal(StatusIdeia.ENVIADA, ideiaDb.Status);
            Assert.Equal(PrioridadeIdeia.MEDIA, ideiaDb.Prioridade);
            Assert.Equal("inovagab-ideia-v1", stored.PromptVersion);
        }

        var historico = await Client.GetAsync($"/api/v1/ideias/{ideia.Id}/analises-ia");
        historico.EnsureSuccessStatusCode();
        var page = await historico.Content.ReadFromJsonAsync<PagedResultDto<AnaliseIaResumoDto>>(IntegrationTestJson.Options);
        Assert.NotNull(page);
        Assert.Equal(1, page!.TotalItems);
    }

    [Fact]
    public async Task Resposta_invalida_da_ia_retorna_502()
    {
        await AuthTestSeed.SeedAsync(Factory.Services);
        _fakeGemini!.Mode = GeminiIdeiaAnalysisMode.InvalidJson;

        var ideiaId = await CriarIdeiaParaGestorAsync();
        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", (await LoginAsync(AuthTestSeed.GestorEmail)).AccessToken);
        var resp = await Client.PostAsync($"/api/v1/ideias/{ideiaId}/analises-ia", null);
        Assert.Equal(HttpStatusCode.BadGateway, resp.StatusCode);
    }

    [Fact]
    public async Task Edicao_da_ideia_marca_analise_desatualizada()
    {
        await AuthTestSeed.SeedAsync(Factory.Services);
        _fakeGemini!.Mode = GeminiIdeiaAnalysisMode.Success;

        var operador = await LoginAsync(AuthTestSeed.OperadorEmail);
        var gestor = await LoginAsync(AuthTestSeed.GestorEmail);
        var ideiaId = await CriarIdeiaParaGestorAsync();

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", gestor.AccessToken);
        var analiseResp = await Client.PostAsync($"/api/v1/ideias/{ideiaId}/analises-ia", null);
        analiseResp.EnsureSuccessStatusCode();
        var analise = (await analiseResp.Content.ReadFromJsonAsync<AnaliseIaDetalheDto>(IntegrationTestJson.Options))!;

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", operador.AccessToken);
        var ideia = await Client.GetFromJsonAsync<IdeiaDetalheDto>($"/api/v1/ideias/{ideiaId}");
        var update = await Client.PutAsJsonAsync($"/api/v1/ideias/{ideiaId}", new IdeiaUpdateRequestDto
        {
            Versao = ideia!.Versao,
            Titulo = "Ideia alterada",
            Descricao = ideia.Descricao,
            Area = ideia.Area
        });
        update.EnsureSuccessStatusCode();

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", gestor.AccessToken);
        var historico = await Client.GetFromJsonAsync<PagedResultDto<AnaliseIaResumoDto>>($"/api/v1/ideias/{ideiaId}/analises-ia");
        Assert.True(historico!.Items.First(i => i.Id == analise.Id).Desatualizada);
    }

    [Fact]
    public async Task Ia_desabilitada_retorna_503()
    {
        EnsureMongo();
        var fake = new FakeGeminiIdeiaAnalysisClient();
        await using var factory = new InovaGabWebApplicationFactory
        {
            MongoConnectionString = MongoTestEnvironment.ConnectionString,
            ConfigureTestServices = services =>
            {
                var descriptor = services.SingleOrDefault(d => d.ServiceType == typeof(IGeminiIdeiaAnalysisClient));
                if (descriptor is not null)
                {
                    services.Remove(descriptor);
                }

                services.AddSingleton<IGeminiIdeiaAnalysisClient>(fake);
            }
        };
        factory.UseSetting("AI:Enabled", "false");
        factory.UseSetting("AI:ApiKey", "");

        await AuthTestSeed.SeedAsync(factory.Services);
        var client = factory.CreateClient();
        var ideiaId = await CriarIdeiaComFactoryAsync(factory, client);

        var gestor = await LoginWithClientAsync(client, AuthTestSeed.GestorEmail);
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", gestor.AccessToken);
        var resp = await client.PostAsync($"/api/v1/ideias/{ideiaId}/analises-ia", null);
        Assert.Equal(HttpStatusCode.ServiceUnavailable, resp.StatusCode);
    }

    private async Task<string> CriarIdeiaParaGestorAsync()
    {
        return await CriarIdeiaComFactoryAsync(_factory!, Client);
    }

    private static async Task<string> CriarIdeiaComFactoryAsync(InovaGabWebApplicationFactory factory, HttpClient client)
    {
        var lider = await LoginWithClientAsync(client, AuthTestSeed.LiderEmail);
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", lider.AccessToken);
        var est = await client.PostAsJsonAsync("/api/v1/estrategias", new EstrategiaCreateRequestDto
        {
            Titulo = "E",
            Descricao = "D",
            Categoria = "C",
            Campanha = "X",
            InicioVigencia = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(-1)),
            FimVigencia = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(10)),
            Ativa = true
        });
        est.EnsureSuccessStatusCode();
        var estrategia = (await est.Content.ReadFromJsonAsync<EstrategiaDetalheDto>(IntegrationTestJson.Options))!;

        var operador = await LoginWithClientAsync(client, AuthTestSeed.OperadorEmail);
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", operador.AccessToken);
        var ideiaResp = await client.PostAsJsonAsync("/api/v1/ideias", new IdeiaCreateRequestDto
        {
            Titulo = "T",
            Descricao = "D",
            Area = "A",
            EstrategiaId = estrategia.Id
        });
        ideiaResp.EnsureSuccessStatusCode();
        var ideia = (await ideiaResp.Content.ReadFromJsonAsync<IdeiaDetalheDto>(IntegrationTestJson.Options))!;
        return ideia.Id;
    }

    private async Task<LoginResponseDto> LoginAsync(string email)
    {
        return await LoginWithClientAsync(Client, email);
    }

    private static async Task<LoginResponseDto> LoginWithClientAsync(HttpClient client, string email)
    {
        var response = await client.PostAsJsonAsync("/api/v1/auth/login", new LoginRequestDto
        {
            Email = email,
            Senha = AuthTestSeed.TestPassword
        });
        response.EnsureSuccessStatusCode();
        return (await response.Content.ReadFromJsonAsync<LoginResponseDto>(IntegrationTestJson.Options))!;
    }

    private static void EnsureMongo() => MongoTestEnvironment.EnsureAvailable();
}
