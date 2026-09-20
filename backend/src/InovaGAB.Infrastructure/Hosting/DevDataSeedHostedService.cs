using InovaGAB.Infrastructure.Persistence;
using InovaGAB.Infrastructure.Seed;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

namespace InovaGAB.Infrastructure.Hosting;

public sealed class DevDataSeedHostedService : IHostedService
{
    private readonly IServiceScopeFactory _scopeFactory;
    private readonly MongoInitializationState _mongoState;
    private readonly ILogger<DevDataSeedHostedService> _logger;

    public DevDataSeedHostedService(
        IServiceScopeFactory scopeFactory,
        MongoInitializationState mongoState,
        ILogger<DevDataSeedHostedService> logger)
    {
        _scopeFactory = scopeFactory;
        _mongoState = mongoState;
        _logger = logger;
    }

    public async Task StartAsync(CancellationToken cancellationToken)
    {
        var timeout = TimeSpan.FromMinutes(2);
        var started = DateTime.UtcNow;
        while (!_mongoState.IsReady && DateTime.UtcNow - started < timeout)
        {
            await Task.Delay(TimeSpan.FromSeconds(1), cancellationToken);
        }

        if (!_mongoState.IsReady)
        {
            _logger.LogWarning("Seed demo não executado: MongoDB não ficou pronto a tempo.");
            return;
        }

        await using var scope = _scopeFactory.CreateAsyncScope();
        var seeder = scope.ServiceProvider.GetRequiredService<DevDataSeeder>();
        await seeder.SeedAsync(cancellationToken);
    }

    public Task StopAsync(CancellationToken cancellationToken) => Task.CompletedTask;
}
