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

        await DropIndexIfExistsAsync(probes, "ix_integration_probes_name", cancellationToken);
        var indexKeys = Builders<MongoDB.Bson.BsonDocument>.IndexKeys.Ascending("Name");
        var indexModel = new CreateIndexModel<MongoDB.Bson.BsonDocument>(
            indexKeys,
            new CreateIndexOptions { Name = "ix_integration_probes_name_v2", Unique = false });

        await probes.Indexes.CreateOneAsync(indexModel, cancellationToken: cancellationToken);

        var usuarios = database.GetCollection<MongoDB.Bson.BsonDocument>("usuarios");
        await DropIndexIfExistsAsync(usuarios, "ux_usuarios_email_normalizado", cancellationToken);
        var usuarioEmailIndex = new CreateIndexModel<MongoDB.Bson.BsonDocument>(
            Builders<MongoDB.Bson.BsonDocument>.IndexKeys.Ascending("EmailNormalizado"),
            new CreateIndexOptions { Name = "ux_usuarios_email_normalizado_v2", Unique = true });
        await usuarios.Indexes.CreateOneAsync(usuarioEmailIndex, cancellationToken: cancellationToken);

        var refreshTokens = database.GetCollection<MongoDB.Bson.BsonDocument>("refresh_tokens");
        await DropIndexIfExistsAsync(refreshTokens, "ux_refresh_tokens_token_hash", cancellationToken);
        var refreshHashIndex = new CreateIndexModel<MongoDB.Bson.BsonDocument>(
            Builders<MongoDB.Bson.BsonDocument>.IndexKeys.Ascending("TokenHash"),
            new CreateIndexOptions { Name = "ux_refresh_tokens_token_hash_v2", Unique = true });
        await refreshTokens.Indexes.CreateOneAsync(refreshHashIndex, cancellationToken: cancellationToken);

        var eventos = database.GetCollection<MongoDB.Bson.BsonDocument>("eventos_pontuacao");
        await DropIndexIfExistsAsync(eventos, "ux_eventos_pontuacao_autor_ideia_tipo", cancellationToken);
        var eventoUnique = new CreateIndexModel<MongoDB.Bson.BsonDocument>(
            Builders<MongoDB.Bson.BsonDocument>.IndexKeys
                .Ascending("AutorId")
                .Ascending("IdeiaId")
                .Ascending("Tipo"),
            new CreateIndexOptions { Name = "ux_eventos_pontuacao_autor_ideia_tipo_v2", Unique = true });
        await eventos.Indexes.CreateOneAsync(eventoUnique, cancellationToken: cancellationToken);

        var projetos = database.GetCollection<MongoDB.Bson.BsonDocument>("projetos");
        await DropIndexIfExistsAsync(projetos, "ux_projetos_ideia_id_parcial", cancellationToken);
        var projetoIdeiaIndex = new CreateIndexModel<MongoDB.Bson.BsonDocument>(
            Builders<MongoDB.Bson.BsonDocument>.IndexKeys.Ascending("IdeiaId"),
            new CreateIndexOptions<MongoDB.Bson.BsonDocument>
            {
                Name = "ux_projetos_ideia_id_parcial_v2",
                Unique = true,
                PartialFilterExpression = new MongoDB.Bson.BsonDocument("IdeiaId", new MongoDB.Bson.BsonDocument
                {
                    { "$exists", true },
                    { "$type", "string" },
                    { "$gt", "" }
                })
            });
        await projetos.Indexes.CreateOneAsync(projetoIdeiaIndex, cancellationToken: cancellationToken);

        _logger.LogInformation(
            "MongoDB indexes ensured for database {Database}",
            _options.DatabaseName);
    }

    private static async Task DropIndexIfExistsAsync(
        IMongoCollection<MongoDB.Bson.BsonDocument> collection,
        string name,
        CancellationToken cancellationToken)
    {
        try
        {
            await collection.Indexes.DropOneAsync(name, cancellationToken);
        }
        catch (MongoCommandException ex) when (ex.CodeName == "IndexNotFound")
        {
            // Fresh databases do not contain the legacy index.
        }
    }
}
