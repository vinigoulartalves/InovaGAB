using InovaGAB.Application.Common;
using InovaGAB.Application.Estrategias;
using InovaGAB.Application.Estrategias.Dtos;
using InovaGAB.Application.Exceptions;
using InovaGAB.Domain.Estrategias;
using InovaGAB.Infrastructure.Common;
using InovaGAB.Infrastructure.Persistence;
using InovaGAB.Infrastructure.Time;
using Microsoft.EntityFrameworkCore;

namespace InovaGAB.Infrastructure.Estrategias;

public sealed class EstrategiaService : IEstrategiaService
{
    private const int MaxDescricao = 4000;
    private const int MaxTitulo = 200;

    private readonly InovaGabDbContext _dbContext;
    private readonly VigenciaEvaluator _vigencia;
    private readonly IVigenciaClock _clock;

    public EstrategiaService(InovaGabDbContext dbContext, VigenciaEvaluator vigencia, IVigenciaClock clock)
    {
        _dbContext = dbContext;
        _vigencia = vigencia;
        _clock = clock;
    }

    public async Task<PagedResultDto<EstrategiaResumoDto>> ListAsync(
        EstrategiaListQuery query,
        CancellationToken cancellationToken)
    {
        var (page, pageSize) = QueryPaging.Normalize(query.Page, query.PageSize);
        var q = _dbContext.Estrategias.AsNoTracking().Where(e => e.ExcluidaEmUtc == null);

        if (!string.IsNullOrWhiteSpace(query.Categoria))
        {
            q = q.Where(e => e.Categoria == query.Categoria);
        }

        if (!string.IsNullOrWhiteSpace(query.Campanha))
        {
            q = q.Where(e => e.Campanha == query.Campanha);
        }

        var items = await q.OrderByDescending(e => e.CriadoEmUtc).ToListAsync(cancellationToken);

        if (query.Vigente is bool vigenteFilter)
        {
            items = items.Where(e => _vigencia.IsVigente(e) == vigenteFilter).ToList();
        }

        var total = items.Count;
        var pageItems = items.Skip((page - 1) * pageSize).Take(pageSize).Select(MapResumo).ToList();

        return new PagedResultDto<EstrategiaResumoDto>
        {
            Items = pageItems,
            Page = page,
            PageSize = pageSize,
            TotalItems = total,
            TotalPages = (int)Math.Ceiling(total / (double)pageSize)
        };
    }

    public async Task<EstrategiaDetalheDto> GetAsync(string id, CancellationToken cancellationToken)
    {
        var entity = await _dbContext.Estrategias.AsNoTracking()
            .FirstOrDefaultAsync(e => e.Id == id, cancellationToken);

        if (entity is null)
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Estratégia não encontrada.");
        }

        return MapDetalhe(entity);
    }

    public async Task<PagedResultDto<EstrategiaHistoricoItemDto>> GetHistoricoAsync(
        string id,
        int page,
        int pageSize,
        CancellationToken cancellationToken)
    {
        var exists = await _dbContext.Estrategias.AnyAsync(e => e.Id == id, cancellationToken);
        if (!exists)
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Estratégia não encontrada.");
        }

        var (p, ps) = QueryPaging.Normalize(page, pageSize);
        var q = _dbContext.EstrategiasHistorico.AsNoTracking()
            .Where(h => h.EstrategiaId == id)
            .OrderByDescending(h => h.OcorridoEmUtc);

        var total = await q.CountAsync(cancellationToken);
        var items = await q.Skip((p - 1) * ps).Take(ps).ToListAsync(cancellationToken);

        return new PagedResultDto<EstrategiaHistoricoItemDto>
        {
            Items = items.Select(MapHistorico).ToList(),
            Page = p,
            PageSize = ps,
            TotalItems = total,
            TotalPages = (int)Math.Ceiling(total / (double)ps)
        };
    }

    public async Task<EstrategiaDetalheDto> CreateAsync(
        EstrategiaCreateRequestDto request,
        string atorId,
        CancellationToken cancellationToken)
    {
        ValidateCreate(request);
        var now = _clock.GetUtcNow();

        var entity = new Estrategia
        {
            Id = Guid.NewGuid().ToString("N"),
            Titulo = request.Titulo.Trim(),
            Descricao = request.Descricao.Trim(),
            Categoria = request.Categoria.Trim(),
            Campanha = request.Campanha.Trim(),
            InicioVigencia = request.InicioVigencia,
            FimVigencia = request.FimVigencia,
            Ativa = request.Ativa,
            Versao = 1,
            CriadoEmUtc = now,
            AtualizadoEmUtc = now
        };

        await using var tx = await _dbContext.Database.BeginTransactionAsync(cancellationToken);
        _dbContext.Estrategias.Add(entity);
        AddHistorico(entity, EstrategiaHistoricoAcao.CRIADA, atorId, now);
        await _dbContext.SaveChangesAsync(cancellationToken);
        await tx.CommitAsync(cancellationToken);

        return MapDetalhe(entity);
    }

    public async Task<EstrategiaDetalheDto> UpdateAsync(
        string id,
        EstrategiaUpdateRequestDto request,
        string atorId,
        CancellationToken cancellationToken)
    {
        ValidateCreate(request);
        var entity = await _dbContext.Estrategias.FirstOrDefaultAsync(e => e.Id == id, cancellationToken);
        if (entity is null || entity.ExcluidaEmUtc is not null)
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Estratégia não encontrada.");
        }

        if (entity.Versao != request.Versao)
        {
            throw new BusinessException(409, "CONCORRENCIA", "A versão informada está desatualizada.");
        }

        var wasAtiva = entity.Ativa;
        entity.Titulo = request.Titulo.Trim();
        entity.Descricao = request.Descricao.Trim();
        entity.Categoria = request.Categoria.Trim();
        entity.Campanha = request.Campanha.Trim();
        entity.InicioVigencia = request.InicioVigencia;
        entity.FimVigencia = request.FimVigencia;
        entity.Ativa = request.Ativa;
        entity.Versao++;
        entity.AtualizadoEmUtc = _clock.GetUtcNow();

        var acao = !request.Ativa && wasAtiva
            ? EstrategiaHistoricoAcao.ARQUIVADA
            : EstrategiaHistoricoAcao.EDITADA;

        await using var tx = await _dbContext.Database.BeginTransactionAsync(cancellationToken);
        AddHistorico(entity, acao, atorId, entity.AtualizadoEmUtc);

        try
        {
            await _dbContext.SaveChangesAsync(cancellationToken);
        }
        catch (DbUpdateConcurrencyException)
        {
            throw new BusinessException(409, "CONCORRENCIA", "A versão informada está desatualizada.");
        }

        await tx.CommitAsync(cancellationToken);
        return MapDetalhe(entity);
    }

    public async Task DeleteLogicalAsync(
        string id,
        int versaoEsperada,
        string atorId,
        CancellationToken cancellationToken)
    {
        var entity = await _dbContext.Estrategias.FirstOrDefaultAsync(e => e.Id == id, cancellationToken);
        if (entity is null || entity.ExcluidaEmUtc is not null)
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Estratégia não encontrada.");
        }

        if (entity.Versao != versaoEsperada)
        {
            throw new BusinessException(409, "CONCORRENCIA", "A versão informada está desatualizada.");
        }

        var now = _clock.GetUtcNow();
        entity.ExcluidaEmUtc = now;
        entity.Versao++;
        entity.AtualizadoEmUtc = now;

        await using var tx = await _dbContext.Database.BeginTransactionAsync(cancellationToken);
        AddHistorico(entity, EstrategiaHistoricoAcao.EXCLUIDA_LOGICAMENTE, atorId, now);
        await _dbContext.SaveChangesAsync(cancellationToken);
        await tx.CommitAsync(cancellationToken);
    }

    private void AddHistorico(Estrategia entity, EstrategiaHistoricoAcao acao, string atorId, DateTime when)
    {
        if (entity.Descricao.Length > MaxDescricao)
        {
            throw new BusinessException(400, "VALIDACAO", "Descrição excede o tamanho máximo.");
        }

        _dbContext.EstrategiasHistorico.Add(new EstrategiaHistorico
        {
            Id = Guid.NewGuid().ToString("N"),
            EstrategiaId = entity.Id,
            Versao = entity.Versao,
            Acao = acao,
            AtorId = atorId,
            OcorridoEmUtc = when,
            Snapshot = new EstrategiaSnapshot
            {
                Titulo = entity.Titulo,
                Descricao = entity.Descricao.Length > MaxDescricao
                    ? entity.Descricao[..MaxDescricao]
                    : entity.Descricao,
                Categoria = entity.Categoria,
                Campanha = entity.Campanha,
                InicioVigencia = entity.InicioVigencia,
                FimVigencia = entity.FimVigencia,
                Ativa = entity.Ativa
            }
        });
    }

    private static void ValidateCreate(EstrategiaCreateRequestDto request)
    {
        if (string.IsNullOrWhiteSpace(request.Titulo) || request.Titulo.Length > MaxTitulo)
        {
            throw new BusinessException(400, "VALIDACAO", "Título inválido.");
        }

        if (request.FimVigencia is not null && request.FimVigencia < request.InicioVigencia)
        {
            throw new BusinessException(400, "VALIDACAO", "Fim de vigência anterior ao início.");
        }
    }

    private EstrategiaResumoDto MapResumo(Estrategia e) => new()
    {
        Id = e.Id,
        Titulo = e.Titulo,
        Categoria = e.Categoria,
        Campanha = e.Campanha,
        InicioVigencia = e.InicioVigencia,
        FimVigencia = e.FimVigencia,
        Ativa = e.Ativa,
        Vigente = _vigencia.IsVigente(e),
        Versao = e.Versao
    };

    private EstrategiaDetalheDto MapDetalhe(Estrategia e)
    {
        var resumo = MapResumo(e);
        return new EstrategiaDetalheDto
        {
            Id = resumo.Id,
            Titulo = resumo.Titulo,
            Categoria = resumo.Categoria,
            Campanha = resumo.Campanha,
            InicioVigencia = resumo.InicioVigencia,
            FimVigencia = resumo.FimVigencia,
            Ativa = resumo.Ativa,
            Vigente = resumo.Vigente,
            Versao = resumo.Versao,
            Descricao = e.Descricao,
            ExcluidaEm = e.ExcluidaEmUtc,
            CriadoEm = e.CriadoEmUtc,
            AtualizadoEm = e.AtualizadoEmUtc
        };
    }

    private static EstrategiaHistoricoItemDto MapHistorico(EstrategiaHistorico h) => new()
    {
        Id = h.Id,
        Versao = h.Versao,
        Acao = h.Acao.ToString(),
        AtorId = h.AtorId,
        OcorridoEm = h.OcorridoEmUtc,
        Snapshot = new EstrategiaSnapshotDto
        {
            Titulo = h.Snapshot.Titulo,
            Descricao = h.Snapshot.Descricao,
            Categoria = h.Snapshot.Categoria,
            Campanha = h.Snapshot.Campanha,
            InicioVigencia = h.Snapshot.InicioVigencia,
            FimVigencia = h.Snapshot.FimVigencia,
            Ativa = h.Snapshot.Ativa
        }
    };
}
