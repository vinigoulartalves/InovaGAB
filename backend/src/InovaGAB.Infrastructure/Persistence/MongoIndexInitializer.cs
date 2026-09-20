using InovaGAB.Infrastructure.Configuration;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using MongoDB.Driver;

namespace InovaGAB.Infrastructure.Persistence;

public sealed class MongoIndexInitializer
{
    private readonly IMongoClient _client;
    private readonly MongoOptions _options;
    private readonly ILogger<MongoIndexInitializer> _logger;

    public MongoIndexInitializer(
        IMongoClient client,
        IOptions<MongoOptions> options,
        ILogger<MongoIndexInitializer> logger)
    {
        _client = client;
        _options = options.Value;
        _logger = logger;
    }

    public async Task InitializeAsync(CancellationToken cancellationToken)
    {
        var database = _client.GetDatabase(_options.DatabaseName);
        var probes = database.GetCollection<MongoDB.Bson.BsonDocument>("integration_probes");

        var indexKeys = Builders<MongoDB.Bson.BsonDocument>.IndexKeys.Ascending("name");
        var indexModel = new CreateIndexModel<MongoDB.Bson.BsonDocument>(
            indexKeys,
            new CreateIndexOptions { Name = "ix_integration_probes_name", Unique = false });

        await probes.Indexes.CreateOneAsync(indexModel, cancellationToken: cancellationToken);

        _logger.LogInformation(
            "MongoDB indexes ensured for database {Database}",
            _options.DatabaseName);
    }
}
