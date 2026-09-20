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

        var usuarios = database.GetCollection<MongoDB.Bson.BsonDocument>("usuarios");
        var usuarioEmailIndex = new CreateIndexModel<MongoDB.Bson.BsonDocument>(
            Builders<MongoDB.Bson.BsonDocument>.IndexKeys.Ascending("emailNormalizado"),
            new CreateIndexOptions { Name = "ux_usuarios_email_normalizado", Unique = true });
        await usuarios.Indexes.CreateOneAsync(usuarioEmailIndex, cancellationToken: cancellationToken);

        var refreshTokens = database.GetCollection<MongoDB.Bson.BsonDocument>("refresh_tokens");
        var refreshHashIndex = new CreateIndexModel<MongoDB.Bson.BsonDocument>(
            Builders<MongoDB.Bson.BsonDocument>.IndexKeys.Ascending("tokenHash"),
            new CreateIndexOptions { Name = "ux_refresh_tokens_token_hash", Unique = true });
        await refreshTokens.Indexes.CreateOneAsync(refreshHashIndex, cancellationToken: cancellationToken);

        var eventos = database.GetCollection<MongoDB.Bson.BsonDocument>("eventos_pontuacao");
        var eventoUnique = new CreateIndexModel<MongoDB.Bson.BsonDocument>(
            Builders<MongoDB.Bson.BsonDocument>.IndexKeys
                .Ascending("autorId")
                .Ascending("ideiaId")
                .Ascending("tipo"),
            new CreateIndexOptions { Name = "ux_eventos_pontuacao_autor_ideia_tipo", Unique = true });
        await eventos.Indexes.CreateOneAsync(eventoUnique, cancellationToken: cancellationToken);

        _logger.LogInformation(
            "MongoDB indexes ensured for database {Database}",
            _options.DatabaseName);
    }
}
