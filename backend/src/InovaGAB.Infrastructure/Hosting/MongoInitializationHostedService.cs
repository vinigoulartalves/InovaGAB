using InovaGAB.Infrastructure.Persistence;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

namespace InovaGAB.Infrastructure.Hosting;

public sealed class MongoInitializationHostedService : IHostedService
{
    private readonly IServiceScopeFactory _scopeFactory;
    private readonly MongoInitializationState _state;
    private readonly ILogger<MongoInitializationHostedService> _logger;

    public MongoInitializationHostedService(
        IServiceScopeFactory scopeFactory,
        MongoInitializationState state,
        ILogger<MongoInitializationHostedService> logger)
    {
        _scopeFactory = scopeFactory;
        _state = state;
        _logger = logger;
    }

    public async Task StartAsync(CancellationToken cancellationToken)
    {
        try
        {
            await using var scope = _scopeFactory.CreateAsyncScope();
            var initializer = scope.ServiceProvider.GetRequiredService<MongoStartupInitializer>();
            await initializer.InitializeAsync(cancellationToken);
            _state.MarkReady();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "MongoDB initialization failed.");
            throw;
        }
    }

    public Task StopAsync(CancellationToken cancellationToken) => Task.CompletedTask;
}
