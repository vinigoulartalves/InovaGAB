using InovaGAB.Infrastructure.Configuration;
using InovaGAB.Infrastructure.Hosting;
using InovaGAB.Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
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

        var mongoOptions = configuration.GetSection(MongoOptions.SectionName).Get<MongoOptions>()
            ?? throw new InvalidOperationException("Mongo configuration is required.");

        if (string.IsNullOrWhiteSpace(mongoOptions.ConnectionString))
        {
            throw new InvalidOperationException("Mongo:ConnectionString is required.");
        }

        services.AddSingleton<IMongoClient>(_ => new MongoClient(mongoOptions.ConnectionString));
        services.AddSingleton<MongoInitializationState>();
        services.AddScoped<MongoIndexInitializer>();
        services.AddScoped<MongoStartupInitializer>();

        services.AddDbContext<InovaGabDbContext>(options =>
        {
            options.UseMongoDB(mongoOptions.ConnectionString, mongoOptions.DatabaseName);
        });

        services.AddHostedService<MongoInitializationHostedService>();

        return services;
    }
}
