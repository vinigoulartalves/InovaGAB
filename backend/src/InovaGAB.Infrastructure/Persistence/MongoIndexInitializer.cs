using InovaGAB.Infrastructure.Configuration;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using MongoDB.Bson;
using MongoDB.Driver;

namespace InovaGAB.Infrastructure.Persistence;

/// <summary>
/// Ensures the MongoDB indexes required by the domain. Field names MUST match the
/// camelCase element names configured in <see cref="InovaGabDbContext"/>.
/// </summary>
public sealed class MongoIndexInitializer
{
    // Indexes created by a previous revision that used PascalCase element names.
    // They point at fields that no longer exist and, being unique, would reject
    // every second insert (all documents share the "missing field" key).
    private static readonly (string Collection, string Index)[] LegacyIndexes =
    {
        ("integration_probes", "ix_integration_probes_name_v2"),
        ("usuarios", "ux_usuarios_email_normalizado_v2"),
        ("refresh_tokens", "ux_refresh_tokens_token_hash_v2"),
        ("eventos_pontuacao", "ux_eventos_pontuacao_autor_ideia_tipo_v2"),
        ("projetos", "ux_projetos_ideia_id_parcial_v2")
    };

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

        foreach (var (collection, index) in LegacyIndexes)
        {
            await DropIndexIfExistsAsync(database.GetCollection<BsonDocument>(collection), index, cancellationToken);
        }

        var probes = database.GetCollection<BsonDocument>("integration_probes");
        await probes.Indexes.CreateOneAsync(
            new CreateIndexModel<BsonDocument>(
                Builders<BsonDocument>.IndexKeys.Ascending("name"),
                new CreateIndexOptions { Name = "ix_integration_probes_name", Unique = false }),
            cancellationToken: cancellationToken);

        var usuarios = database.GetCollection<BsonDocument>("usuarios");
        await usuarios.Indexes.CreateOneAsync(
            new CreateIndexModel<BsonDocument>(
                Builders<BsonDocument>.IndexKeys.Ascending("emailNormalizado"),
                new CreateIndexOptions { Name = "ux_usuarios_email_normalizado", Unique = true }),
            cancellationToken: cancellationToken);

        var refreshTokens = database.GetCollection<BsonDocument>("refresh_tokens");
        await refreshTokens.Indexes.CreateOneAsync(
            new CreateIndexModel<BsonDocument>(
                Builders<BsonDocument>.IndexKeys.Ascending("tokenHash"),
                new CreateIndexOptions { Name = "ux_refresh_tokens_token_hash", Unique = true }),
            cancellationToken: cancellationToken);

        var eventos = database.GetCollection<BsonDocument>("eventos_pontuacao");
        await eventos.Indexes.CreateOneAsync(
            new CreateIndexModel<BsonDocument>(
                Builders<BsonDocument>.IndexKeys
                    .Ascending("autorId")
                    .Ascending("ideiaId")
                    .Ascending("tipo"),
                new CreateIndexOptions { Name = "ux_eventos_pontuacao_autor_ideia_tipo", Unique = true }),
            cancellationToken: cancellationToken);

        var projetos = database.GetCollection<BsonDocument>("projetos");
        await projetos.Indexes.CreateOneAsync(
            new CreateIndexModel<BsonDocument>(
                Builders<BsonDocument>.IndexKeys.Ascending("ideiaId"),
                new CreateIndexOptions<BsonDocument>
                {
                    Name = "ux_projetos_ideia_id_parcial",
                    Unique = true,
                    PartialFilterExpression = new BsonDocument("ideiaId", new BsonDocument
                    {
                        { "$exists", true },
                        { "$type", "string" },
                        { "$gt", "" }
                    })
                }),
            cancellationToken: cancellationToken);

        _logger.LogInformation(
            "MongoDB indexes ensured for database {Database}",
            _options.DatabaseName);
    }

    private async Task DropIndexIfExistsAsync(
        IMongoCollection<BsonDocument> collection,
        string name,
        CancellationToken cancellationToken)
    {
        try
        {
            await collection.Indexes.DropOneAsync(name, cancellationToken);
            _logger.LogInformation(
                "Legacy index {Index} removed from {Collection}",
                name,
                collection.CollectionNamespace.CollectionName);
        }
        catch (MongoCommandException ex) when (ex.CodeName is "IndexNotFound" or "NamespaceNotFound")
        {
            // Fresh databases contain neither the collection nor the legacy index.
        }
    }
}
