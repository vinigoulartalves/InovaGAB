using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.Configuration;
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

        var settings = new Dictionary<string, string?>
        {
            ["Mongo:ConnectionString"] = MongoConnectionString ?? string.Empty,
            ["Mongo:DatabaseName"] = DatabaseName,
            ["Jwt:Secret"] = "integration-test-secret-min-32-characters-long!",
            ["Jwt:Issuer"] = "inovagab-test",
            ["Jwt:Audience"] = "inovagab-test",
            ["Jwt:AccessTokenMinutes"] = "15",
            ["Jwt:RefreshTokenDays"] = "7",
            ["Seed:Enabled"] = "false",
            ["AI:Enabled"] = "false"
        };

        // Per-test settings must win over the safe defaults above.
        foreach (var (key, value) in _settings)
        {
            settings[key] = value ?? string.Empty;
        }

        // Host configuration: available while Program.cs registers services.
        foreach (var (key, value) in settings)
        {
            builder.UseSetting(key, value);
        }

        // Appended as the LAST application configuration source so the same values
        // also win over appsettings.*.json and environment variables such as
        // Mongo__DatabaseName exported by docker compose. Every test therefore runs
        // indexes, seed and queries inside its own isolated database.
        builder.ConfigureAppConfiguration((_, configuration) =>
            configuration.AddInMemoryCollection(settings));

        builder.ConfigureServices(services => ConfigureTestServices?.Invoke(services));
    }
}
