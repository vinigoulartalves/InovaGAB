using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using InovaGAB.Application.Auth.Dtos;
using InovaGAB.Application.Estrategias.Dtos;
using InovaGAB.Application.Ideias.Dtos;
using InovaGAB.Domain.Ideias;
using InovaGAB.Domain.Pontuacao;
using InovaGAB.Infrastructure.Persistence;
using InovaGAB.IntegrationTests.Infrastructure;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Xunit;

namespace InovaGAB.IntegrationTests.EstrategiasIdeias;

public sealed class EstrategiasIdeiasTests
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
    public async Task Fluxo_estrategia_ideia_pontuacao_e_permissoes()
    {
        await AuthTestSeed.SeedAsync(_factory!.Services);

        var lider = await LoginAsync(AuthTestSeed.LiderEmail);
        var op1 = await LoginAsync(AuthTestSeed.OperadorEmail);
        var op2 = await LoginAsync(AuthTestSeed.Operador2Email);
        var gestor = await LoginAsync(AuthTestSeed.GestorEmail);

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", lider.AccessToken);
        var estrategia = await Client.PostAsJsonAsync("/api/v1/estrategias", new EstrategiaCreateRequestDto
        {
            Titulo = "Estratégia vigente teste",
            Descricao = "Desc",
            Categoria = "Cat",
            Campanha = "Camp",
            InicioVigencia = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(-1)),
            FimVigencia = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(30)),
            Ativa = true
        });
        estrategia.EnsureSuccessStatusCode();
        var est = (await estrategia.Content.ReadFromJsonAsync<EstrategiaDetalheDto>())!;

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", op1.AccessToken);
        var ideiaResp = await Client.PostAsJsonAsync("/api/v1/ideias", new IdeiaCreateRequestDto
        {
            Titulo = "Ideia A",
            Descricao = "D",
            Area = "Ops",
            EstrategiaId = est.Id
        });
        ideiaResp.EnsureSuccessStatusCode();
        var ideia = (await ideiaResp.Content.ReadFromJsonAsync<IdeiaDetalheDto>())!;

        await using (var scope = _factory!.Services.CreateAsyncScope())
        {
            var db = scope.ServiceProvider.GetRequiredService<InovaGabDbContext>();
            var eventos = await db.EventosPontuacao.CountAsync(
                e => e.IdeiaId == ideia.Id && e.Tipo == TipoEventoPontuacao.CADASTRO);
            Assert.Equal(1, eventos);
        }

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", op2.AccessToken);
        var forbidden = await Client.GetAsync($"/api/v1/ideias/{ideia.Id}");
        Assert.Equal(HttpStatusCode.NotFound, forbidden.StatusCode);

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", gestor.AccessToken);
        var avaliacao = await Client.PatchAsJsonAsync($"/api/v1/ideias/{ideia.Id}/avaliacao", new IdeiaAvaliacaoRequestDto
        {
            Versao = ideia.Versao,
            Status = StatusIdeia.APROVADA,
            Prioridade = PrioridadeIdeia.ALTA
        });
        avaliacao.EnsureSuccessStatusCode();

        await using (var scope = _factory.Services.CreateAsyncScope())
        {
            var db = scope.ServiceProvider.GetRequiredService<InovaGabDbContext>();
            var pontos = await db.EventosPontuacao
                .Where(e => e.IdeiaId == ideia.Id)
                .SumAsync(e => e.Pontos);
            Assert.Equal(40, pontos);
        }

        var reAvalia = await Client.PatchAsJsonAsync($"/api/v1/ideias/{ideia.Id}/avaliacao", new IdeiaAvaliacaoRequestDto
        {
            Versao = ideia.Versao + 1,
            Status = StatusIdeia.APROVADA
        });
        reAvalia.EnsureSuccessStatusCode();

        await using (var scope = _factory.Services.CreateAsyncScope())
        {
            var db = scope.ServiceProvider.GetRequiredService<InovaGabDbContext>();
            var aprovacoes = await db.EventosPontuacao.CountAsync(
                e => e.IdeiaId == ideia.Id && e.Tipo == TipoEventoPontuacao.PRIMEIRA_APROVACAO);
            Assert.Equal(1, aprovacoes);
        }

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", lider.AccessToken);
        var historico = await Client.GetAsync($"/api/v1/estrategias/{est.Id}/historico");
        historico.EnsureSuccessStatusCode();
    }

    [Fact]
    public async Task Ideia_rejeita_estrategia_nao_vigente()
    {
        await AuthTestSeed.SeedAsync(_factory!.Services);
        var lider = await LoginAsync(AuthTestSeed.LiderEmail);
        var op1 = await LoginAsync(AuthTestSeed.OperadorEmail);

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", lider.AccessToken);
        var estrategia = await Client.PostAsJsonAsync("/api/v1/estrategias", new EstrategiaCreateRequestDto
        {
            Titulo = "Vencida",
            Descricao = "D",
            Categoria = "C",
            Campanha = "C",
            InicioVigencia = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(-60)),
            FimVigencia = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(-1)),
            Ativa = true
        });
        estrategia.EnsureSuccessStatusCode();
        var est = (await estrategia.Content.ReadFromJsonAsync<EstrategiaDetalheDto>())!;

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", op1.AccessToken);
        var ideiaResp = await Client.PostAsJsonAsync("/api/v1/ideias", new IdeiaCreateRequestDto
        {
            Titulo = "X",
            Descricao = "Y",
            Area = "Z",
            EstrategiaId = est.Id
        });
        Assert.Equal(HttpStatusCode.Conflict, ideiaResp.StatusCode);
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

    private static void EnsureMongo()
    {
        if (!MongoTestEnvironment.IsAvailable)
        {
            throw new InvalidOperationException("MongoDB indisponível. Defina MONGODB_URI.");
        }
    }
}
