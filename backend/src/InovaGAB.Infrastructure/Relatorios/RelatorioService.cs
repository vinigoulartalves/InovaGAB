using InovaGAB.Application.Exceptions;
using InovaGAB.Application.Relatorios;
using InovaGAB.Application.Relatorios.Dtos;
using InovaGAB.Infrastructure.Persistence;
using InovaGAB.Infrastructure.Projetos;
using InovaGAB.Infrastructure.Time;
using Microsoft.EntityFrameworkCore;

namespace InovaGAB.Infrastructure.Relatorios;

public sealed class RelatorioService : IRelatorioService
{
    private readonly RelatorioRepository _repository;
    private readonly InovaGabDbContext _dbContext;
    private readonly IVigenciaClock _clock;

    public RelatorioService(
        RelatorioRepository repository,
        InovaGabDbContext dbContext,
        IVigenciaClock clock)
    {
        _repository = repository;
        _dbContext = dbContext;
        _clock = clock;
    }

    public async Task<DashboardRelatorioDto> GetDashboardAsync(
        DashboardFiltroDto filtro,
        CancellationToken cancellationToken)
    {
        var agg = await _repository.AggregateAsync(filtro, cancellationToken);
        return MapDashboard(agg);
    }

    public async Task<IReadOnlyList<RelatorioEstrategiaItemDto>> GetPorEstrategiaAsync(
        DashboardFiltroDto filtro,
        CancellationToken cancellationToken)
    {
        var agg = await _repository.AggregateAsync(filtro, cancellationToken);
        return agg.PorEstrategia.Select(e =>
        {
            var lucro = RelatorioCalculos.Lucro(e.Investimento, e.Retorno);
            return new RelatorioEstrategiaItemDto
            {
                EstrategiaId = e.EstrategiaId,
                Titulo = e.Titulo,
                Investimento = RelatorioCalculos.RoundMoney(e.Investimento),
                Retorno = RelatorioCalculos.RoundMoney(e.Retorno),
                Lucro = RelatorioCalculos.RoundMoney(lucro),
                RoiPercentual = RelatorioCalculos.RoiPercentual(e.Investimento, lucro),
                QuantidadeProjetos = e.Quantidade
            };
        }).ToList();
    }

    public async Task<RelatorioProjetoDetalheDto> GetProjetoAsync(
        string projetoId,
        CancellationToken cancellationToken)
    {
        var entity = await _dbContext.Projetos.AsNoTracking()
            .FirstOrDefaultAsync(p => p.Id == projetoId && p.ExcluidaEmUtc == null, cancellationToken);

        if (entity is null)
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Projeto não encontrado.");
        }

        var responsavelNome = await _dbContext.Usuarios.AsNoTracking()
            .Where(u => u.Id == entity.ResponsavelId)
            .Select(u => u.Nome)
            .FirstOrDefaultAsync(cancellationToken) ?? "";

        var detalhe = ProjetoMapper.ToDetalhe(entity, responsavelNome);
        var lucro = RelatorioCalculos.Lucro(entity.Investimento, entity.RetornoFinanceiro);
        var hoje = _clock.GetTodaySaoPaulo();
        var atrasado = entity.Prazo < hoje
            && entity.Status is not Domain.Projetos.StatusProjeto.CONCLUIDO
            && entity.Status is not Domain.Projetos.StatusProjeto.CANCELADO;

        return new RelatorioProjetoDetalheDto
        {
            Projeto = detalhe,
            Lucro = RelatorioCalculos.RoundMoney(lucro),
            RoiPercentual = RelatorioCalculos.RoiPercentual(entity.Investimento, lucro),
            Atrasado = atrasado
        };
    }

    private static DashboardRelatorioDto MapDashboard(RelatorioRepository.RelatorioAgregado agg)
    {
        var lucro = RelatorioCalculos.Lucro(agg.InvestimentoTotal, agg.RetornoTotal);
        var roi = RelatorioCalculos.RoiPercentual(agg.InvestimentoTotal, lucro);
        decimal? mediaProdutividade = agg.ProjetoCount > 0
            ? RelatorioCalculos.RoundMoney(agg.GanhoProdutividadeSum / agg.ProjetoCount)
            : null;

        return new DashboardRelatorioDto
        {
            InvestimentoTotal = RelatorioCalculos.RoundMoney(agg.InvestimentoTotal),
            RetornoTotal = RelatorioCalculos.RoundMoney(agg.RetornoTotal),
            LucroTotal = RelatorioCalculos.RoundMoney(lucro),
            RoiPercentual = roi,
            ReducaoCustosTotal = RelatorioCalculos.RoundMoney(agg.ReducaoCustosTotal),
            GanhoProdutividadeMedio = mediaProdutividade,
            ProjetosAtrasados = agg.ProjetosAtrasados,
            InvestimentoRetornoPorEstrategia = agg.PorEstrategia.Select(e => new SerieInvestimentoRetornoDto
            {
                EstrategiaId = e.EstrategiaId,
                EstrategiaTitulo = e.Titulo,
                Investimento = RelatorioCalculos.RoundMoney(e.Investimento),
                Retorno = RelatorioCalculos.RoundMoney(e.Retorno)
            }).ToList(),
            DistribuicaoPorStatus = agg.PorStatus.Select(s => new DistribuicaoStatusDto
            {
                Status = s.Status,
                Quantidade = s.Quantidade
            }).ToList()
        };
    }
}
