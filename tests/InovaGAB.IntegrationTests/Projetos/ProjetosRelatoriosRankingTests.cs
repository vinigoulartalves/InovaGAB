using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using InovaGAB.Application.Auth.Dtos;
using InovaGAB.Application.Estrategias.Dtos;
using InovaGAB.Application.Ideias.Dtos;
using InovaGAB.Application.Projetos.Dtos;
using InovaGAB.Application.Ranking.Dtos;
using InovaGAB.Application.Relatorios.Dtos;
using InovaGAB.Domain.Ideias;
using InovaGAB.Domain.Projetos;
using InovaGAB.Infrastructure.Auth;
using InovaGAB.Infrastructure.Persistence;
using InovaGAB.IntegrationTests.Infrastructure;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Xunit;

namespace InovaGAB.IntegrationTests.Projetos;

public sealed class ProjetosRelatoriosRankingTests
{
    private InovaGabWebApplicationFactory? _factory;
    private HttpClient? _client;

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
            _factory ??= new InovaGabWebApplicationFactory
            {
                MongoConnectionString = MongoTestEnvironment.ConnectionString
            };
            return _client ??= _factory.CreateClient();
        }
    }

    [Fact]
    public async Task Crud_projeto_conversao_relatorios_ranking_e_regras()
    {
        await AuthTestSeed.SeedAsync(Factory.Services);

        var gestor = await LoginAsync(AuthTestSeed.GestorEmail);
        var lider = await LoginAsync(AuthTestSeed.LiderEmail);
        var operador = await LoginAsync(AuthTestSeed.OperadorEmail);

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", lider.AccessToken);
        var estResp = await Client.PostAsJsonAsync("/api/v1/estrategias", new EstrategiaCreateRequestDto
        {
            Titulo = "Estratégia projetos teste",
            Descricao = "D",
            Categoria = "C",
            Campanha = "PRJ",
            InicioVigencia = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(-2)),
            FimVigencia = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(60)),
            Ativa = true
        });
        estResp.EnsureSuccessStatusCode();
        var estrategia = (await estResp.Content.ReadFromJsonAsync<EstrategiaDetalheDto>(IntegrationTestJson.Options))!;

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", operador.AccessToken);
        Assert.Equal(HttpStatusCode.Forbidden, (await Client.GetAsync("/api/v1/projetos")).StatusCode);

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", gestor.AccessToken);
        var rejectIdeiaId = await Client.PostAsJsonAsync("/api/v1/projetos", new
        {
            ideiaId = "fake",
            nome = "X",
            descricao = "Y",
            estrategiaId = estrategia.Id,
            responsavelId = gestor.Usuario.Id,
            etapa = "E",
            status = StatusProjeto.PLANEJADO,
            investimento = 1,
            retornoFinanceiro = 2,
            reducaoCustos = 0,
            ganhoProdutividade = 1,
            prazo = DateOnly.FromDateTime(DateTime.UtcNow.AddMonths(1))
        });
        Assert.Equal(HttpStatusCode.BadRequest, rejectIdeiaId.StatusCode);

        var gestorId = await GetGestorIdAsync();
        var createA = await Client.PostAsJsonAsync("/api/v1/projetos", new ProjetoCreateRequestDto
        {
            Nome = "Projeto A teste",
            Descricao = "A",
            EstrategiaId = estrategia.Id,
            ResponsavelId = gestorId,
            Etapa = "Exec",
            Status = StatusProjeto.EM_ANDAMENTO,
            Investimento = 1000,
            RetornoFinanceiro = 1500,
            Prazo = DateOnly.FromDateTime(DateTime.UtcNow.AddMonths(2))
        });
        createA.EnsureSuccessStatusCode();
        var projetoA = (await createA.Content.ReadFromJsonAsync<ProjetoDetalheDto>(IntegrationTestJson.Options))!;

        var createB = await Client.PostAsJsonAsync("/api/v1/projetos", new ProjetoCreateRequestDto
        {
            Nome = "Projeto B teste",
            Descricao = "B",
            EstrategiaId = estrategia.Id,
            ResponsavelId = gestorId,
            Etapa = "Exec",
            Status = StatusProjeto.EM_ANDAMENTO,
            Investimento = 2000,
            RetornoFinanceiro = 2600,
            Prazo = DateOnly.FromDateTime(DateTime.UtcNow.AddMonths(2))
        });
        createB.EnsureSuccessStatusCode();

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", lider.AccessToken);
        var listLider = await Client.GetAsync("/api/v1/projetos");
        listLider.EnsureSuccessStatusCode();

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", gestor.AccessToken);
        var invalidResp = await Client.PostAsJsonAsync("/api/v1/projetos", new ProjetoCreateRequestDto
        {
            Nome = "Inválido",
            Descricao = "D",
            EstrategiaId = estrategia.Id,
            ResponsavelId = operador.Usuario.Id,
            Etapa = "E",
            Status = StatusProjeto.PLANEJADO,
            Investimento = 10,
            RetornoFinanceiro = 20,
            Prazo = DateOnly.FromDateTime(DateTime.UtcNow.AddMonths(1))
        });
        Assert.Equal(HttpStatusCode.BadRequest, invalidResp.StatusCode);

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", lider.AccessToken);
        var dash = await Client.GetAsync($"/api/v1/relatorios/dashboard?estrategiaId={estrategia.Id}");
        dash.EnsureSuccessStatusCode();
        var dashboard = (await dash.Content.ReadFromJsonAsync<DashboardRelatorioDto>(IntegrationTestJson.Options))!;
        Assert.Equal(3000m, dashboard.InvestimentoTotal);
        Assert.Equal(4100m, dashboard.RetornoTotal);
        Assert.Equal(1100m, dashboard.LucroTotal);
        Assert.Equal(36.6667m, dashboard.RoiPercentual);

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", gestor.AccessToken);
        var gestorDash = await Client.GetAsync("/api/v1/relatorios/dashboard");
        Assert.Equal(HttpStatusCode.Forbidden, gestorDash.StatusCode);

        var zeroInv = await Client.PostAsJsonAsync("/api/v1/projetos", new ProjetoCreateRequestDto
        {
            Nome = "Zero investimento",
            Descricao = "Z",
            EstrategiaId = estrategia.Id,
            ResponsavelId = gestorId,
            Etapa = "E",
            Status = StatusProjeto.PLANEJADO,
            Investimento = 0,
            RetornoFinanceiro = 100,
            Prazo = DateOnly.FromDateTime(DateTime.UtcNow.AddMonths(1))
        });
        zeroInv.EnsureSuccessStatusCode();
        var zeroProj = (await zeroInv.Content.ReadFromJsonAsync<ProjetoDetalheDto>(IntegrationTestJson.Options))!;

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", lider.AccessToken);
        var relZero = await Client.GetAsync($"/api/v1/relatorios/projetos/{zeroProj.Id}");
        relZero.EnsureSuccessStatusCode();
        var relZeroDto = (await relZero.Content.ReadFromJsonAsync<RelatorioProjetoDetalheDto>(IntegrationTestJson.Options))!;
        Assert.Null(relZeroDto.RoiPercentual);

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", gestor.AccessToken);
        var op2 = await LoginAsync(AuthTestSeed.Operador2Email);
        var ideiaAprov = await CriarIdeiaAprovadaAsync(op2, estrategia.Id);

        var conv = await Client.PostAsJsonAsync($"/api/v1/ideias/{ideiaAprov.Id}/projeto", new ConversaoIdeiaProjetoRequestDto
        {
            Versao = ideiaAprov.Versao,
            Nome = "Convertido",
            Descricao = "C",
            ResponsavelId = gestorId,
            Etapa = "P",
            Status = StatusProjeto.PLANEJADO,
            Investimento = 50,
            RetornoFinanceiro = 80,
            Prazo = DateOnly.FromDateTime(DateTime.UtcNow.AddMonths(1))
        });
        conv.EnsureSuccessStatusCode();

        var dup = await Client.PostAsJsonAsync($"/api/v1/ideias/{ideiaAprov.Id}/projeto", new ConversaoIdeiaProjetoRequestDto
        {
            Versao = ideiaAprov.Versao + 1,
            Nome = "Dup",
            Descricao = "D",
            ResponsavelId = gestorId,
            Etapa = "P",
            Status = StatusProjeto.PLANEJADO,
            Investimento = 1,
            RetornoFinanceiro = 2,
            Prazo = DateOnly.FromDateTime(DateTime.UtcNow.AddMonths(1))
        });
        Assert.Equal(HttpStatusCode.Conflict, dup.StatusCode);

        var deleteReq = new HttpRequestMessage(HttpMethod.Delete, $"/api/v1/projetos/{projetoA.Id}");
        deleteReq.Headers.TryAddWithoutValidation("If-Match", $"W/\"{projetoA.Versao}\"");
        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", gestor.AccessToken);
        var deleted = await Client.SendAsync(deleteReq);
        deleted.EnsureSuccessStatusCode();

        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", lider.AccessToken);
        var dashAfterDelete = await Client.GetAsync($"/api/v1/relatorios/dashboard?estrategiaId={estrategia.Id}");
        dashAfterDelete.EnsureSuccessStatusCode();
        var dash2 = (await dashAfterDelete.Content.ReadFromJsonAsync<DashboardRelatorioDto>(IntegrationTestJson.Options))!;
        Assert.Equal(2050m, dash2.InvestimentoTotal);

        var rankingResp = await Client.GetAsync("/api/v1/ranking");
        rankingResp.EnsureSuccessStatusCode();
        var ranking = (await rankingResp.Content.ReadFromJsonAsync<RankingResponseDto>(IntegrationTestJson.Options))!;
        Assert.True(ranking.Items.Count >= 1);
        Assert.DoesNotContain(ranking.Items, i => i.Nome.Contains('@'));
    }

    private async Task<IdeiaDetalheDto> CriarIdeiaAprovadaAsync(LoginResponseDto operador, string estrategiaId)
    {
        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", operador.AccessToken);
        var ideiaResp = await Client.PostAsJsonAsync("/api/v1/ideias", new IdeiaCreateRequestDto
        {
            Titulo = "Para conversão",
            Descricao = "D",
            Area = "A",
            EstrategiaId = estrategiaId
        });
        ideiaResp.EnsureSuccessStatusCode();
        var ideia = (await ideiaResp.Content.ReadFromJsonAsync<IdeiaDetalheDto>(IntegrationTestJson.Options))!;

        var gestor = await LoginAsync(AuthTestSeed.GestorEmail);
        Client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", gestor.AccessToken);
        var aval = await Client.PatchAsJsonAsync($"/api/v1/ideias/{ideia.Id}/avaliacao", new IdeiaAvaliacaoRequestDto
        {
            Versao = ideia.Versao,
            Status = StatusIdeia.APROVADA
        });
        aval.EnsureSuccessStatusCode();
        return (await aval.Content.ReadFromJsonAsync<IdeiaDetalheDto>(IntegrationTestJson.Options))!;
    }

    private async Task<string> GetGestorIdAsync()
    {
        await using var scope = Factory.Services.CreateAsyncScope();
        var db = scope.ServiceProvider.GetRequiredService<InovaGabDbContext>();
        return await db.Usuarios
            .Where(u => u.EmailNormalizado == AuthService.NormalizeEmail(AuthTestSeed.GestorEmail))
            .Select(u => u.Id)
            .FirstAsync();
    }

    private async Task<LoginResponseDto> LoginAsync(string email)
    {
        var response = await Client.PostAsJsonAsync("/api/v1/auth/login", new LoginRequestDto
        {
            Email = email,
            Senha = AuthTestSeed.TestPassword
        });
        response.EnsureSuccessStatusCode();
        return (await response.Content.ReadFromJsonAsync<LoginResponseDto>(IntegrationTestJson.Options))!;
    }

    private static void EnsureMongo() => MongoTestEnvironment.EnsureAvailable();
}
