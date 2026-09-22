using InovaGAB.Infrastructure.Configuration;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using MongoDB.Bson;
using MongoDB.Bson.Serialization;
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
        var probes = database.GetCollection<BsonDocument>("integration_probes");

        // EF Core Mongo persiste nomes de propriedade C# (PascalCase), não camelCase JSON.
        await EnsureIndexAsync(
            probes,
            new CreateIndexModel<BsonDocument>(
                Builders<BsonDocument>.IndexKeys.Ascending("Name"),
                new CreateIndexOptions { Name = "ix_integration_probes_name", Unique = false }),
            cancellationToken);

        var usuarios = database.GetCollection<BsonDocument>("usuarios");
        await EnsureIndexAsync(
            usuarios,
            new CreateIndexModel<BsonDocument>(
                Builders<BsonDocument>.IndexKeys.Ascending("EmailNormalizado"),
                new CreateIndexOptions { Name = "ux_usuarios_email_normalizado", Unique = true }),
            cancellationToken);

        var refreshTokens = database.GetCollection<BsonDocument>("refresh_tokens");
        await EnsureIndexAsync(
            refreshTokens,
            new CreateIndexModel<BsonDocument>(
                Builders<BsonDocument>.IndexKeys.Ascending("TokenHash"),
                new CreateIndexOptions { Name = "ux_refresh_tokens_token_hash", Unique = true }),
            cancellationToken);

        var eventos = database.GetCollection<BsonDocument>("eventos_pontuacao");
        await EnsureIndexAsync(
            eventos,
            new CreateIndexModel<BsonDocument>(
                Builders<BsonDocument>.IndexKeys
                    .Ascending("AutorId")
                    .Ascending("IdeiaId")
                    .Ascending("Tipo"),
                new CreateIndexOptions { Name = "ux_eventos_pontuacao_autor_ideia_tipo", Unique = true }),
            cancellationToken);

        var projetos = database.GetCollection<BsonDocument>("projetos");
        await EnsureIndexAsync(
            projetos,
            new CreateIndexModel<BsonDocument>(
                Builders<BsonDocument>.IndexKeys.Ascending("IdeiaId"),
                new CreateIndexOptions<BsonDocument>
                {
                    Name = "ux_projetos_ideia_id_parcial",
                    Unique = true,
                    PartialFilterExpression = new BsonDocument("IdeiaId", new BsonDocument
                    {
                        { "$exists", true },
                        { "$type", "string" },
                        { "$gt", "" }
                    })
                }),
            cancellationToken);

        _logger.LogInformation(
            "MongoDB indexes ensured for database {Database}",
            _options.DatabaseName);
    }

    private async Task EnsureIndexAsync(
        IMongoCollection<BsonDocument> collection,
        CreateIndexModel<BsonDocument> model,
        CancellationToken cancellationToken)
    {
        var indexName = model.Options?.Name
            ?? throw new InvalidOperationException("Index name is required for managed Mongo indexes.");

        var renderArgs = new RenderArgs<BsonDocument>(
            collection.DocumentSerializer,
            BsonSerializer.SerializerRegistry);
        var expectedKey = model.Keys.Render(renderArgs);

        using var cursor = await collection.Indexes.ListAsync(cancellationToken);
        var existingIndexes = await cursor.ToListAsync(cancellationToken);
        var existing = existingIndexes.FirstOrDefault(doc =>
            doc.TryGetValue("name", out var nameValue) && nameValue.AsString == indexName);

        if (existing is not null)
        {
            var existingKey = existing["key"].AsBsonDocument;
            var keysMatch = existingKey.Equals(expectedKey);
            var filtersMatch = PartialFiltersMatch(existing, model.Options);

            if (keysMatch && filtersMatch)
            {
                return;
            }

            _logger.LogWarning(
                "Replacing legacy Mongo index {IndexName} on {Collection} (expected keys {ExpectedKey}, found {ExistingKey}).",
                indexName,
                collection.CollectionNamespace.CollectionName,
                expectedKey.ToJson(),
                existingKey.ToJson());

            await collection.Indexes.DropOneAsync(indexName, cancellationToken);
        }

        await collection.Indexes.CreateOneAsync(model, cancellationToken: cancellationToken);
    }

    private static bool PartialFiltersMatch(BsonDocument existingIndex, CreateIndexOptions? options)
    {
        existingIndex.TryGetValue("partialFilterExpression", out var existingFilter);
        var expectedFilter = options switch
        {
            CreateIndexOptions<BsonDocument> typed when typed.PartialFilterExpression is not null =>
                typed.PartialFilterExpression,
            _ => null
        };

        if (expectedFilter is null)
        {
            return existingFilter is null || existingFilter.IsBsonNull;
        }

        if (existingFilter is null || existingFilter.IsBsonNull)
        {
            return false;
        }

        return existingFilter.AsBsonDocument.Equals(expectedFilter);
    }
}
