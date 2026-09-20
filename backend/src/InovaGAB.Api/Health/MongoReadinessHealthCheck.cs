using InovaGAB.Infrastructure.Persistence;
using Microsoft.Extensions.Diagnostics.HealthChecks;
using MongoDB.Driver;

namespace InovaGAB.Api.Health;

public sealed class MongoReadinessHealthCheck : IHealthCheck
{
    private readonly IMongoClient _client;
    private readonly MongoInitializationState _initializationState;
    private readonly IConfiguration _configuration;

    public MongoReadinessHealthCheck(
        IMongoClient client,
        MongoInitializationState initializationState,
        IConfiguration configuration)
    {
        _client = client;
        _initializationState = initializationState;
        _configuration = configuration;
    }

    public async Task<HealthCheckResult> CheckHealthAsync(
        HealthCheckContext context,
        CancellationToken cancellationToken = default)
    {
        if (!_initializationState.IsReady)
        {
            return HealthCheckResult.Unhealthy("MongoDB initialization not completed.");
        }

        var databaseName = _configuration["Mongo:DatabaseName"] ?? "inovagab";
        var ping = new MongoDB.Bson.BsonDocument("ping", 1);
        await _client.GetDatabase(databaseName).RunCommandAsync<MongoDB.Bson.BsonDocument>(
            ping,
            cancellationToken: cancellationToken);

        return HealthCheckResult.Healthy("MongoDB reachable and initialized.");
    }
}
