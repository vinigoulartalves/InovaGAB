using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using InovaGAB.Application.Auth.Dtos;
using InovaGAB.Application.Estrategias.Dtos;
using InovaGAB.Application.Ideias.Dtos;
using InovaGAB.Application.Projetos.Dtos;
using InovaGAB.Application.Ranking.Dtos;
using InovaGAB.Application.Relatorios.Dtos;
using InovaGAB.Domain.Ideias;
using InovaGAB.Domain.Projetos;
using InovaGAB.IntegrationTests.Infrastructure;
using Xunit;

namespace InovaGAB.IntegrationTests.E2E;

/// <summary>
/// Jornada HTTP única (login real, Mongo rs0, WebApplicationFactory).
/// </summary>
public sealed class Sprint2JornadaHttpE2ETests
{
    [Fact]
    public async Task Jornada_completa_lider_operadores_gestor_dashboard_ranking()
    {
        MongoTestEnvironment.EnsureAvailable();
        await using var factory = new InovaGabWebApplicationFactory
        {
            MongoConnectionString = MongoTestEnvironment.ConnectionString
        };
        var client = factory.CreateClient();
        await AuthTestSeed.SeedAsync(factory.Services);

        var lider = await LoginAsync(client, AuthTestSeed.LiderEmail);
        var op1 = await LoginAsync(client, AuthTestSeed.OperadorEmail);
        var op2 = await LoginAsync(client, AuthTestSeed.Operador2Email);
        var gestor = await LoginAsync(client, AuthTestSeed.GestorEmail);

        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", lider.AccessToken);
        var estResp = await client.PostAsJsonAsync("/api/v1/estrategias", new EstrategiaCreateRequestDto
        {
            Titulo = "E2E Estratégia",
            Descricao = "Jornada Sprint 2",
            Categoria = "Inovação",
            Campanha = "E2E",
            InicioVigencia = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(-1)),
            FimVigencia = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(90)),
            Ativa = true
        });
        estResp.EnsureSuccessStatusCode();
        var estrategia = (await estResp.Content.ReadFromJsonAsync<EstrategiaDetalheDto>(IntegrationTestJson.Options))!;

        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", op1.AccessToken);
        var ideiaResp = await client.PostAsJsonAsync("/api/v1/ideias", new IdeiaCreateRequestDto
        {
            Titulo = "Ideia E2E",
            Descricao = "Fluxo completo",
            Area = "Operações",
            EstrategiaId = estrategia.Id
        });
        ideiaResp.EnsureSuccessStatusCode();
        var ideia = (await ideiaResp.Content.ReadFromJsonAsync<IdeiaDetalheDto>(IntegrationTestJson.Options))!;

        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", op2.AccessToken);
        Assert.Equal(HttpStatusCode.NotFound, (await client.GetAsync($"/api/v1/ideias/{ideia.Id}")).StatusCode);
        var editOutroOperador = await client.PutAsJsonAsync($"/api/v1/ideias/{ideia.Id}", new
        {
            versao = ideia.Versao,
            titulo = "Hack"
        });
        Assert.False(editOutroOperador.IsSuccessStatusCode);

        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", gestor.AccessToken);
        var aprovar = await client.PatchAsJsonAsync($"/api/v1/ideias/{ideia.Id}/avaliacao", new IdeiaAvaliacaoRequestDto
        {
            Versao = ideia.Versao,
            Status = StatusIdeia.APROVADA,
            Prioridade = PrioridadeIdeia.ALTA
        });
        aprovar.EnsureSuccessStatusCode();
        var ideiaAprovada = (await aprovar.Content.ReadFromJsonAsync<IdeiaDetalheDto>(IntegrationTestJson.Options))!;

        var gestorId = gestor.Usuario.Id;
        var conversao = await client.PostAsJsonAsync($"/api/v1/ideias/{ideiaAprovada.Id}/projeto", new
        {
            versao = ideiaAprovada.Versao,
            nome = "Projeto E2E",
            descricao = "Da ideia",
            responsavelId = gestorId,
            etapa = "Planejamento",
            status = StatusProjeto.PLANEJADO,
            investimento = 1000m,
            retornoFinanceiro = 1400m,
            reducaoCustos = 0m,
            ganhoProdutividade = 5m,
            prazo = DateOnly.FromDateTime(DateTime.UtcNow.AddMonths(3))
        });
        conversao.EnsureSuccessStatusCode();
        var projeto = (await conversao.Content.ReadFromJsonAsync<ProjetoDetalheDto>(IntegrationTestJson.Options))!;

        var segundaConversao = await client.PostAsJsonAsync($"/api/v1/ideias/{ideiaAprovada.Id}/projeto", new { versao = 99 });
        Assert.Equal(HttpStatusCode.Conflict, segundaConversao.StatusCode);

        var update = await client.PutAsJsonAsync($"/api/v1/projetos/{projeto.Id}", new ProjetoUpdateRequestDto
        {
            Versao = projeto.Versao,
            Nome = projeto.Nome,
            Descricao = projeto.Descricao,
            ResponsavelId = gestorId,
            Etapa = "Execução",
            Status = StatusProjeto.EM_ANDAMENTO,
            Investimento = 1000,
            RetornoFinanceiro = 2000,
            ReducaoCustos = 0,
            GanhoProdutividade = 10,
            Prazo = projeto.Prazo
        });
        update.EnsureSuccessStatusCode();

        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", lider.AccessToken);
        var dash = await client.GetAsync("/api/v1/relatorios/dashboard");
        dash.EnsureSuccessStatusCode();
        var relatorio = (await dash.Content.ReadFromJsonAsync<DashboardRelatorioDto>(IntegrationTestJson.Options))!;
        Assert.True(relatorio.InvestimentoTotal >= 1000);

        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", op1.AccessToken);
        var minha = await client.GetAsync($"/api/v1/ideias/{ideia.Id}");
        minha.EnsureSuccessStatusCode();
        var ideiaAtual = (await minha.Content.ReadFromJsonAsync<IdeiaDetalheDto>(IntegrationTestJson.Options))!;
        Assert.Equal(StatusIdeia.VIROU_PROJETO, ideiaAtual.Status);

        var ranking = await client.GetAsync("/api/v1/ranking");
        ranking.EnsureSuccessStatusCode();
        var rank = (await ranking.Content.ReadFromJsonAsync<RankingResponseDto>(IntegrationTestJson.Options))!;
        Assert.NotEmpty(rank.Items);
    }

    private static async Task<LoginResponseDto> LoginAsync(HttpClient client, string email)
    {
        var response = await client.PostAsJsonAsync("/api/v1/auth/login", new LoginRequestDto
        {
            Email = email,
            Senha = AuthTestSeed.TestPassword
        });
        response.EnsureSuccessStatusCode();
        return (await response.Content.ReadFromJsonAsync<LoginResponseDto>(IntegrationTestJson.Options))!;
    }
}
