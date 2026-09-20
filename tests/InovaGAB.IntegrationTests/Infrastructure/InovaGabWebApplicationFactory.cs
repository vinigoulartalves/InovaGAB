using InovaGAB.Infrastructure.Persistence;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;

namespace InovaGAB.IntegrationTests.Infrastructure;

public sealed class InovaGabWebApplicationFactory : WebApplicationFactory<Program>
{
    public string? MongoConnectionString { get; init; }

    public string DatabaseName { get; init; } = $"inovagab_auth_test_{Guid.NewGuid():N}";

    public Action<IServiceCollection>? ConfigureTestServices { get; init; }

    private readonly Dictionary<string, string?> _settings = new();

    public void UseSetting(string key, string? value) => _settings[key] = value;

    protected override void ConfigureWebHost(IWebHostBuilder builder)
    {
        builder.UseEnvironment("Development");

        foreach (var (key, value) in _settings)
        {
            builder.UseSetting(key, value ?? string.Empty);
        }

        builder.ConfigureServices(services =>
        {
            ConfigureTestServices?.Invoke(services);

            if (string.IsNullOrWhiteSpace(MongoConnectionString))
            {
                return;
            }

            var descriptors = services
                .Where(d => d.ServiceType == typeof(DbContextOptions<InovaGabDbContext>)
                    || d.ServiceType == typeof(InovaGabDbContext))
                .ToList();

            foreach (var descriptor in descriptors)
            {
                services.Remove(descriptor);
            }

            services.AddDbContext<InovaGabDbContext>(options =>
            {
                options.UseMongoDB(MongoConnectionString, DatabaseName);
            });
        });

        builder.UseSetting("Mongo:ConnectionString", MongoConnectionString ?? string.Empty);
        builder.UseSetting("Mongo:DatabaseName", DatabaseName);
        builder.UseSetting("Jwt:Secret", "integration-test-secret-min-32-characters-long!");
        builder.UseSetting("Jwt:Issuer", "inovagab-test");
        builder.UseSetting("Jwt:Audience", "inovagab-test");
        builder.UseSetting("Jwt:AccessTokenMinutes", "15");
        builder.UseSetting("Jwt:RefreshTokenDays", "7");
        builder.UseSetting("Seed:Enabled", "false");
        builder.UseSetting("AI:Enabled", "false");
    }
}
