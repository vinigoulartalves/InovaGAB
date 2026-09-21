using InovaGAB.Application.Ranking;
using InovaGAB.Application.Ranking.Dtos;
using InovaGAB.Infrastructure.Persistence;
using InovaGAB.Infrastructure.Time;
using Microsoft.EntityFrameworkCore;

namespace InovaGAB.Infrastructure.Ranking;

public sealed class RankingService : IRankingService
{
    private readonly InovaGabDbContext _dbContext;
    private readonly IVigenciaClock _clock;

    public RankingService(InovaGabDbContext dbContext, IVigenciaClock clock)
    {
        _dbContext = dbContext;
        _clock = clock;
    }

    public async Task<RankingResponseDto> GetRankingAsync(CancellationToken cancellationToken)
    {
        var pontosPorAutor = await _dbContext.EventosPontuacao.AsNoTracking()
            .GroupBy(e => e.AutorId)
            .Select(g => new { AutorId = g.Key, Pontos = g.Sum(e => e.Pontos) })
            .ToListAsync(cancellationToken);

        var autorIds = pontosPorAutor.Select(p => p.AutorId).ToList();
        var usuarios = await _dbContext.Usuarios.AsNoTracking()
            .Where(u => autorIds.Contains(u.Id))
            .Select(u => new { u.Id, u.Nome })
            .ToListAsync(cancellationToken);

        var nomePorId = usuarios.ToDictionary(u => u.Id, u => u.Nome);

        var ordenado = pontosPorAutor
            .Select(p => new
            {
                p.AutorId,
                p.Pontos,
                Nome = nomePorId.GetValueOrDefault(p.AutorId, "Desconhecido")
            })
            .OrderByDescending(x => x.Pontos)
            .ThenBy(x => x.Nome, StringComparer.OrdinalIgnoreCase)
            .ThenBy(x => x.AutorId, StringComparer.Ordinal)
            .ToList();

        var items = new List<RankingItemDto>();
        for (var i = 0; i < ordenado.Count; i++)
        {
            items.Add(new RankingItemDto
            {
                Posicao = i + 1,
                Nome = ordenado[i].Nome,
                Pontos = ordenado[i].Pontos
            });
        }

        return new RankingResponseDto
        {
            Items = items,
            AtualizadoEm = _clock.GetUtcNow()
        };
    }
}
