using InovaGAB.Application.Auth;
using InovaGAB.Application.Estrategias;
using InovaGAB.Application.Ideias;
using InovaGAB.Application.Projetos;
using InovaGAB.Application.Ranking;
using InovaGAB.Application.Relatorios;
using InovaGAB.Domain.Usuarios;
using InovaGAB.Infrastructure.Auth;
using InovaGAB.Infrastructure.Configuration;
using InovaGAB.Infrastructure.Hosting;
using InovaGAB.Infrastructure.Persistence;
using InovaGAB.Infrastructure.Estrategias;
using InovaGAB.Infrastructure.Ia;
using InovaGAB.Infrastructure.Ideias;
using InovaGAB.Infrastructure.Projetos;
using InovaGAB.Infrastructure.Ranking;
using InovaGAB.Infrastructure.Relatorios;
using InovaGAB.Infrastructure.Pontuacao;
using InovaGAB.Infrastructure.Seed;
using InovaGAB.Infrastructure.Time;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Options;
using MongoDB.Driver;

namespace InovaGAB.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(
        this IServiceCollection services,
        IConfiguration configuration)
    {
        services.Configure<MongoOptions>(configuration.GetSection(MongoOptions.SectionName));
        services.Configure<JwtOptions>(configuration.GetSection(JwtOptions.SectionName));
        services.Configure<AiOptions>(configuration.GetSection(AiOptions.SectionName));
        services.Configure<SeedOptions>(configuration.GetSection(SeedOptions.SectionName));

        // Mongo settings are resolved lazily through IOptions so that every configuration
        // source (appsettings, environment variables, test overrides) is honoured, and so
        // the DbContext shares the single IMongoClient instance.
        services.AddSingleton<IMongoClient>(sp =>
        {
            var mongo = sp.GetRequiredService<IOptions<MongoOptions>>().Value;
            if (string.IsNullOrWhiteSpace(mongo.ConnectionString))
            {
                throw new InvalidOperationException("Mongo:ConnectionString is required.");
            }

            return new MongoClient(mongo.ConnectionString);
        });
        services.AddSingleton<MongoInitializationState>();
        services.AddScoped<MongoIndexInitializer>();
        services.AddScoped<MongoStartupInitializer>();

        services.AddDbContext<InovaGabDbContext>((sp, options) =>
        {
            var mongo = sp.GetRequiredService<IOptions<MongoOptions>>().Value;
            options.UseMongoDB(sp.GetRequiredService<IMongoClient>(), mongo.DatabaseName);
        });

        services.AddHostedService<MongoInitializationHostedService>();
        services.AddHostedService<DevDataSeedHostedService>();

        services.AddSingleton<TimeProvider>(TimeProvider.System);
        services.AddSingleton<PasswordHasher<Usuario>>();
        services.AddScoped<JwtAccessTokenFactory>();
        services.AddScoped<IAuthService, AuthService>();
        services.AddScoped<DevDataSeeder>();
        services.AddSingleton<IVigenciaClock, VigenciaClock>();
        services.AddScoped<VigenciaEvaluator>();
        services.AddScoped<PontuacaoService>();
        services.AddScoped<IEstrategiaService, EstrategiaService>();
        services.AddScoped<IIdeiaService, IdeiaService>();
        services.AddScoped<IProjetoService, ProjetoService>();
        services.AddScoped<RelatorioRepository>();
        services.AddScoped<IRelatorioService, RelatorioService>();
        services.AddScoped<IRankingService, RankingService>();
        services.AddScoped<IIdeaAnalysisService, IdeiaAnalysisService>();
        services.AddScoped<IGeminiIdeiaAnalysisClient, GeminiIdeiaAnalysisClient>();

        services.AddHttpClient(GeminiIdeiaAnalysisClient.HttpClientName, (sp, client) =>
        {
            var ai = sp.GetRequiredService<IOptions<AiOptions>>().Value;
            client.BaseAddress = ai.BaseUrl;
            client.Timeout = TimeSpan.FromSeconds(Math.Max(5, ai.TimeoutSeconds));
        });

        return services;
    }
}
