using InovaGAB.Application.Relatorios.Dtos;
using InovaGAB.Domain.Projetos;
using InovaGAB.Infrastructure.Configuration;
using InovaGAB.Infrastructure.Time;
using Microsoft.Extensions.Options;
using MongoDB.Bson;
using MongoDB.Driver;

namespace InovaGAB.Infrastructure.Relatorios;

public sealed class RelatorioRepository
{
    private readonly IMongoDatabase _database;
    private readonly IVigenciaClock _clock;

    public RelatorioRepository(IMongoClient client, IOptions<MongoOptions> options, IVigenciaClock clock)
    {
        _database = client.GetDatabase(options.Value.DatabaseName);
        _clock = clock;
    }

    public async Task<RelatorioAgregado> AggregateAsync(DashboardFiltroDto filtro, CancellationToken cancellationToken)
    {
        var match = BuildMatch(filtro);
        var collection = _database.GetCollection<BsonDocument>("projetos");

        var totalsPipeline = new[]
        {
            new BsonDocument("$match", match),
            new BsonDocument("$group", new BsonDocument
            {
                { "_id", BsonNull.Value },
                { "investimento", new BsonDocument("$sum", "$Investimento") },
                { "retorno", new BsonDocument("$sum", "$RetornoFinanceiro") },
                { "reducaoCustos", new BsonDocument("$sum", "$ReducaoCustos") },
                { "ganhoProdutividadeSum", new BsonDocument("$sum", "$GanhoProdutividade") },
                { "count", new BsonDocument("$sum", 1) }
            })
        };

        var totalsCursor = await collection.AggregateAsync<BsonDocument>(
            totalsPipeline,
            cancellationToken: cancellationToken);
        var totalsDoc = await totalsCursor.FirstOrDefaultAsync(cancellationToken);

        var investimento = totalsDoc is null ? 0m : ToDecimal(totalsDoc["investimento"]);
        var retorno = totalsDoc is null ? 0m : ToDecimal(totalsDoc["retorno"]);
        var reducao = totalsDoc is null ? 0m : ToDecimal(totalsDoc["reducaoCustos"]);
        var ganhoSum = totalsDoc is null ? 0m : ToDecimal(totalsDoc["ganhoProdutividadeSum"]);
        var count = totalsDoc is null ? 0 : totalsDoc["count"].AsInt32;

        var porEstrategiaPipeline = new[]
        {
            new BsonDocument("$match", match),
            new BsonDocument("$group", new BsonDocument
            {
                { "_id", "$EstrategiaId" },
                { "investimento", new BsonDocument("$sum", "$Investimento") },
                { "retorno", new BsonDocument("$sum", "$RetornoFinanceiro") },
                { "quantidade", new BsonDocument("$sum", 1) }
            }),
            new BsonDocument("$sort", new BsonDocument("_id", 1))
        };

        var porEstrategia = await collection
            .Aggregate<BsonDocument>(porEstrategiaPipeline, cancellationToken: cancellationToken)
            .ToListAsync(cancellationToken);

        var estrategiaIds = porEstrategia.Select(d => d["_id"].AsString).ToList();
        var titulos = await LoadEstrategiaTitulosAsync(estrategiaIds, cancellationToken);

        var statusPipeline = new[]
        {
            new BsonDocument("$match", match),
            new BsonDocument("$group", new BsonDocument
            {
                { "_id", "$Status" },
                { "quantidade", new BsonDocument("$sum", 1) }
            })
        };

        var statusDocs = await collection
            .Aggregate<BsonDocument>(statusPipeline, cancellationToken: cancellationToken)
            .ToListAsync(cancellationToken);

        var hoje = _clock.GetTodaySaoPaulo();
        var atrasados = await CountAtrasadosAsync(match, hoje, cancellationToken);

        return new RelatorioAgregado
        {
            InvestimentoTotal = investimento,
            RetornoTotal = retorno,
            ReducaoCustosTotal = reducao,
            GanhoProdutividadeSum = ganhoSum,
            ProjetoCount = count,
            PorEstrategia = porEstrategia.Select(d =>
            {
                var id = d["_id"].AsString;
                return new RelatorioEstrategiaAgg
                {
                    EstrategiaId = id,
                    Titulo = titulos.GetValueOrDefault(id, id),
                    Investimento = ToDecimal(d["investimento"]),
                    Retorno = ToDecimal(d["retorno"]),
                    Quantidade = d["quantidade"].AsInt32
                };
            }).ToList(),
            PorStatus = statusDocs.Select(d => new DistribuicaoStatusAgg
            {
                Status = ToStatusProjeto(d["_id"]),
                Quantidade = d["quantidade"].AsInt32
            }).ToList(),
            ProjetosAtrasados = atrasados
        };
    }

    private async Task<int> CountAtrasadosAsync(BsonDocument match, DateOnly hoje, CancellationToken cancellationToken)
    {
        var prazoLimite = hoje.ToString("yyyy-MM-dd");
        var combined = new BsonDocument("$and", new BsonArray
        {
            match,
            new BsonDocument("Prazo", new BsonDocument("$lt", prazoLimite)),
            new BsonDocument("Status", new BsonDocument("$nin", new BsonArray
            {
                (int)StatusProjeto.CONCLUIDO,
                (int)StatusProjeto.CANCELADO,
                "CONCLUIDO",
                "CANCELADO"
            }))
        });

        var collection = _database.GetCollection<BsonDocument>("projetos");
        return (int)await collection.CountDocumentsAsync(combined, cancellationToken: cancellationToken);
    }

    private async Task<Dictionary<string, string>> LoadEstrategiaTitulosAsync(
        IReadOnlyList<string> ids,
        CancellationToken cancellationToken)
    {
        if (ids.Count == 0)
        {
            return new Dictionary<string, string>();
        }

        var estrategias = _database.GetCollection<BsonDocument>("estrategias");
        var filter = Builders<BsonDocument>.Filter.In("_id", ids);
        var docs = await estrategias.Find(filter).ToListAsync(cancellationToken);
        return docs.ToDictionary(
            d => d["_id"].AsString,
            d => d.GetValue("Titulo", "").AsString);
    }

    private static BsonDocument BuildMatch(DashboardFiltroDto filtro)
    {
        var clauses = new BsonArray
        {
            new BsonDocument("$or", new BsonArray
            {
                new BsonDocument("ExcluidaEmUtc", BsonNull.Value),
                new BsonDocument("ExcluidaEmUtc", new BsonDocument("$exists", false))
            })
        };

        if (!string.IsNullOrWhiteSpace(filtro.EstrategiaId))
        {
            clauses.Add(new BsonDocument("EstrategiaId", filtro.EstrategiaId));
        }

        if (!string.IsNullOrWhiteSpace(filtro.ProjetoId))
        {
            clauses.Add(new BsonDocument("_id", filtro.ProjetoId));
        }

        if (filtro.Inicio is not null)
        {
            var start = filtro.Inicio.Value.ToDateTime(TimeOnly.MinValue, DateTimeKind.Utc);
            clauses.Add(new BsonDocument("CriadoEmUtc", new BsonDocument("$gte", start)));
        }

        if (filtro.Fim is not null)
        {
            var end = filtro.Fim.Value.AddDays(1).ToDateTime(TimeOnly.MinValue, DateTimeKind.Utc);
            clauses.Add(new BsonDocument("CriadoEmUtc", new BsonDocument("$lt", end)));
        }

        return new BsonDocument("$and", clauses);
    }

    private static decimal ToDecimal(BsonValue value)
    {
        return value.BsonType switch
        {
            BsonType.Decimal128 => Decimal128.ToDecimal(value.AsDecimal128),
            BsonType.Double => (decimal)value.AsDouble,
            BsonType.Int32 => value.AsInt32,
            BsonType.Int64 => value.AsInt64,
            _ => 0m
        };
    }

    private static StatusProjeto ToStatusProjeto(BsonValue value) => value.BsonType switch
    {
        BsonType.String => Enum.Parse<StatusProjeto>(value.AsString),
        BsonType.Int32 => (StatusProjeto)value.AsInt32,
        BsonType.Int64 => (StatusProjeto)value.AsInt64,
        _ => throw new InvalidOperationException($"Status de projeto BSON inválido: {value.BsonType}.")
    };

    public sealed class RelatorioAgregado
    {
        public decimal InvestimentoTotal { get; init; }

        public decimal RetornoTotal { get; init; }

        public decimal ReducaoCustosTotal { get; init; }

        public decimal GanhoProdutividadeSum { get; init; }

        public int ProjetoCount { get; init; }

        public int ProjetosAtrasados { get; init; }

        public IReadOnlyList<RelatorioEstrategiaAgg> PorEstrategia { get; init; } = Array.Empty<RelatorioEstrategiaAgg>();

        public IReadOnlyList<DistribuicaoStatusAgg> PorStatus { get; init; } = Array.Empty<DistribuicaoStatusAgg>();
    }

    public sealed class RelatorioEstrategiaAgg
    {
        public string EstrategiaId { get; init; } = string.Empty;

        public string Titulo { get; init; } = string.Empty;

        public decimal Investimento { get; init; }

        public decimal Retorno { get; init; }

        public int Quantidade { get; init; }
    }

    public sealed class DistribuicaoStatusAgg
    {
        public StatusProjeto Status { get; init; }

        public int Quantidade { get; init; }
    }
}
