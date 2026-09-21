using InovaGAB.Application.Relatorios.Dtos;
using InovaGAB.Domain.Projetos;
using InovaGAB.Infrastructure.Persistence;
using InovaGAB.Infrastructure.Time;
using Microsoft.EntityFrameworkCore;

namespace InovaGAB.Infrastructure.Relatorios;

public sealed class RelatorioRepository
{
    private readonly InovaGabDbContext _dbContext;
    private readonly IVigenciaClock _clock;

    public RelatorioRepository(InovaGabDbContext dbContext, IVigenciaClock clock)
    {
        _dbContext = dbContext;
        _clock = clock;
    }

    public async Task<RelatorioAgregado> AggregateAsync(DashboardFiltroDto filtro, CancellationToken cancellationToken)
    {
        var projetos = await _dbContext.Projetos.AsNoTracking().ToListAsync(cancellationToken);
        var filtrados = projetos
            .Where(p => p.ExcluidaEmUtc is null)
            .Where(p => string.IsNullOrWhiteSpace(filtro.EstrategiaId) || p.EstrategiaId == filtro.EstrategiaId)
            .Where(p => string.IsNullOrWhiteSpace(filtro.ProjetoId) || p.Id == filtro.ProjetoId)
            .Where(p => filtro.Inicio is null || DateOnly.FromDateTime(p.CriadoEmUtc) >= filtro.Inicio.Value)
            .Where(p => filtro.Fim is null || DateOnly.FromDateTime(p.CriadoEmUtc) <= filtro.Fim.Value)
            .ToList();

        var estrategias = await _dbContext.Estrategias.AsNoTracking().ToListAsync(cancellationToken);
        var titulos = estrategias.ToDictionary(e => e.Id, e => e.Titulo);
        var hoje = _clock.GetTodaySaoPaulo();

        return new RelatorioAgregado
        {
            InvestimentoTotal = filtrados.Sum(p => p.Investimento),
            RetornoTotal = filtrados.Sum(p => p.RetornoFinanceiro),
            ReducaoCustosTotal = filtrados.Sum(p => p.ReducaoCustos),
            GanhoProdutividadeSum = filtrados.Sum(p => p.GanhoProdutividade),
            ProjetoCount = filtrados.Count,
            PorEstrategia = filtrados.GroupBy(p => p.EstrategiaId).OrderBy(g => g.Key).Select(g =>
            {
                return new RelatorioEstrategiaAgg
                {
                    EstrategiaId = g.Key,
                    Titulo = titulos.GetValueOrDefault(g.Key, g.Key),
                    Investimento = g.Sum(p => p.Investimento),
                    Retorno = g.Sum(p => p.RetornoFinanceiro),
                    Quantidade = g.Count()
                };
            }).ToList(),
            PorStatus = filtrados.GroupBy(p => p.Status).Select(g => new DistribuicaoStatusAgg
            {
                Status = g.Key,
                Quantidade = g.Count()
            }).ToList(),
            ProjetosAtrasados = filtrados.Count(p =>
                p.Prazo < hoje && p.Status is not StatusProjeto.CONCLUIDO and not StatusProjeto.CANCELADO)
        };
    }

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
