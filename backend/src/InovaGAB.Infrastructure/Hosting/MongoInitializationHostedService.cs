using InovaGAB.Infrastructure.Persistence;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

namespace InovaGAB.Infrastructure.Hosting;

public sealed class MongoInitializationHostedService : IHostedService
{
    private readonly MongoStartupInitializer _initializer;
    private readonly MongoInitializationState _state;
    private readonly ILogger<MongoInitializationHostedService> _logger;

    public MongoInitializationHostedService(
        MongoStartupInitializer initializer,
        MongoInitializationState state,
        ILogger<MongoInitializationHostedService> logger)
    {
        _initializer = initializer;
        _state = state;
        _logger = logger;
    }

    public async Task StartAsync(CancellationToken cancellationToken)
    {
        try
        {
            await _initializer.InitializeAsync(cancellationToken);
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
