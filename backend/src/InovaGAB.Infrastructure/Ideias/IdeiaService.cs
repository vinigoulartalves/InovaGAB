using InovaGAB.Application.Common;
using InovaGAB.Application.Exceptions;
using InovaGAB.Application.Ideias;
using InovaGAB.Application.Ideias.Dtos;
using InovaGAB.Domain.Ideias;
using InovaGAB.Domain.Pontuacao;
using InovaGAB.Domain.Usuarios;
using InovaGAB.Infrastructure.Common;
using InovaGAB.Infrastructure.Estrategias;
using InovaGAB.Infrastructure.Persistence;
using InovaGAB.Infrastructure.Ia;
using InovaGAB.Infrastructure.Pontuacao;
using InovaGAB.Infrastructure.Time;
using Microsoft.EntityFrameworkCore;

namespace InovaGAB.Infrastructure.Ideias;

public sealed class IdeiaService : IIdeiaService
{
    private readonly InovaGabDbContext _dbContext;
    private readonly VigenciaEvaluator _vigencia;
    private readonly IVigenciaClock _clock;
    private readonly PontuacaoService _pontuacao;

    public IdeiaService(
        InovaGabDbContext dbContext,
        VigenciaEvaluator vigencia,
        IVigenciaClock clock,
        PontuacaoService pontuacao)
    {
        _dbContext = dbContext;
        _vigencia = vigencia;
        _clock = clock;
        _pontuacao = pontuacao;
    }

    public async Task<PagedResultDto<IdeiaResumoDto>> ListAsync(
        IdeiaListQuery query,
        string usuarioId,
        PerfilUsuario perfil,
        CancellationToken cancellationToken)
    {
        if (perfil == PerfilUsuario.LIDER)
        {
            throw new BusinessException(403, "ACESSO_NEGADO", "Perfil sem permissão para ideias.");
        }

        var (page, pageSize) = QueryPaging.Normalize(query.Page, query.PageSize);
        var q = _dbContext.Ideias.AsNoTracking().Where(i => i.ExcluidaEmUtc == null);

        if (perfil == PerfilUsuario.OPERADOR)
        {
            q = q.Where(i => i.AutorId == usuarioId);
        }
        else if (perfil == PerfilUsuario.GESTOR && !string.IsNullOrWhiteSpace(query.AutorId))
        {
            q = q.Where(i => i.AutorId == query.AutorId);
        }

        if (query.Status is not null)
        {
            q = q.Where(i => i.Status == query.Status);
        }

        if (query.Prioridade is not null)
        {
            q = q.Where(i => i.Prioridade == query.Prioridade);
        }

        if (!string.IsNullOrWhiteSpace(query.EstrategiaId))
        {
            q = q.Where(i => i.EstrategiaId == query.EstrategiaId);
        }

        var total = await q.CountAsync(cancellationToken);
        var entities = await q
            .OrderByDescending(i => i.CriadoEmUtc)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync(cancellationToken);

        var autorIds = entities.Select(e => e.AutorId).Distinct().ToList();
        var nomes = await _dbContext.Usuarios.AsNoTracking()
            .Where(u => autorIds.Contains(u.Id))
            .ToDictionaryAsync(u => u.Id, u => u.Nome, cancellationToken);

        return new PagedResultDto<IdeiaResumoDto>
        {
            Items = entities.Select(e => MapResumo(e, nomes.GetValueOrDefault(e.AutorId, ""))).ToList(),
            Page = page,
            PageSize = pageSize,
            TotalItems = total,
            TotalPages = (int)Math.Ceiling(total / (double)pageSize)
        };
    }

    public async Task<IdeiaDetalheDto> GetAsync(
        string id,
        string usuarioId,
        PerfilUsuario perfil,
        CancellationToken cancellationToken)
    {
        if (perfil == PerfilUsuario.LIDER)
        {
            throw new BusinessException(403, "ACESSO_NEGADO", "Perfil sem permissão para ideias.");
        }

        var entity = await _dbContext.Ideias.AsNoTracking()
            .FirstOrDefaultAsync(i => i.Id == id && i.ExcluidaEmUtc == null, cancellationToken);

        if (entity is null || !CanAccess(entity, usuarioId, perfil))
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Ideia não encontrada.");
        }

        var nome = await _dbContext.Usuarios.AsNoTracking()
            .Where(u => u.Id == entity.AutorId)
            .Select(u => u.Nome)
            .FirstOrDefaultAsync(cancellationToken) ?? "";

        return MapDetalhe(entity, nome);
    }

    public async Task<IdeiaDetalheDto> CreateAsync(
        IdeiaCreateRequestDto request,
        string autorId,
        CancellationToken cancellationToken)
    {
        var estrategia = await _dbContext.Estrategias.AsNoTracking()
            .FirstOrDefaultAsync(e => e.Id == request.EstrategiaId, cancellationToken);

        if (estrategia is null)
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Estratégia não encontrada.");
        }

        if (!_vigencia.IsVigente(estrategia))
        {
            throw new BusinessException(409, "ESTRATEGIA_NAO_VIGENTE", "Estratégia não está vigente para novos vínculos.");
        }

        var now = _clock.GetUtcNow();
        var ideia = new Ideia
        {
            Id = Guid.NewGuid().ToString("N"),
            Titulo = request.Titulo.Trim(),
            Descricao = request.Descricao.Trim(),
            Area = request.Area.Trim(),
            AutorId = autorId,
            Status = StatusIdeia.ENVIADA,
            Prioridade = PrioridadeIdeia.MEDIA,
            EstrategiaId = estrategia.Id,
            EstrategiaVersao = estrategia.Versao,
            Versao = 1,
            CriadoEmUtc = now,
            AtualizadoEmUtc = now
        };

        await using var tx = await _dbContext.Database.BeginTransactionAsync(cancellationToken);
        _dbContext.Ideias.Add(ideia);
        await _dbContext.SaveChangesAsync(cancellationToken);

        _dbContext.EventosPontuacao.Add(new EventoPontuacao
        {
            Id = Guid.NewGuid().ToString("N"),
            AutorId = autorId,
            IdeiaId = ideia.Id,
            Tipo = TipoEventoPontuacao.CADASTRO,
            Pontos = 10,
            OcorridoEmUtc = now
        });
        await _dbContext.SaveChangesAsync(cancellationToken);
        await tx.CommitAsync(cancellationToken);

        var nome = await _dbContext.Usuarios.AsNoTracking()
            .Where(u => u.Id == autorId)
            .Select(u => u.Nome)
            .FirstAsync(cancellationToken);

        return MapDetalhe(ideia, nome);
    }

    public async Task<IdeiaDetalheDto> UpdateAsync(
        string id,
        IdeiaUpdateRequestDto request,
        string autorId,
        CancellationToken cancellationToken)
    {
        var entity = await _dbContext.Ideias.FirstOrDefaultAsync(i => i.Id == id, cancellationToken);
        if (entity is null || entity.ExcluidaEmUtc is not null || entity.AutorId != autorId)
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Ideia não encontrada.");
        }

        if (entity.Status != StatusIdeia.ENVIADA)
        {
            throw new BusinessException(409, "STATUS_INVALIDO", "Ideia não pode ser editada neste status.");
        }

        if (entity.Versao != request.Versao)
        {
            throw new BusinessException(409, "CONCORRENCIA", "A versão informada está desatualizada.");
        }

        entity.Titulo = request.Titulo.Trim();
        entity.Descricao = request.Descricao.Trim();
        entity.Area = request.Area.Trim();
        entity.Versao++;
        entity.AtualizadoEmUtc = _clock.GetUtcNow();

        await IdeiaAnalysisService.MarcarDesatualizadasAsync(_dbContext, entity.Id, cancellationToken);

        try
        {
            await _dbContext.SaveChangesAsync(cancellationToken);
        }
        catch (DbUpdateConcurrencyException)
        {
            throw new BusinessException(409, "CONCORRENCIA", "A versão informada está desatualizada.");
        }

        var nome = await _dbContext.Usuarios.AsNoTracking()
            .Where(u => u.Id == autorId)
            .Select(u => u.Nome)
            .FirstAsync(cancellationToken);

        return MapDetalhe(entity, nome);
    }

    public async Task DeleteLogicalAsync(
        string id,
        int versaoEsperada,
        string autorId,
        CancellationToken cancellationToken)
    {
        var entity = await _dbContext.Ideias.FirstOrDefaultAsync(i => i.Id == id, cancellationToken);
        if (entity is null || entity.ExcluidaEmUtc is not null || entity.AutorId != autorId)
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Ideia não encontrada.");
        }

        if (entity.Status != StatusIdeia.ENVIADA)
        {
            throw new BusinessException(409, "STATUS_INVALIDO", "Ideia não pode ser excluída neste status.");
        }

        if (entity.Versao != versaoEsperada)
        {
            throw new BusinessException(409, "CONCORRENCIA", "A versão informada está desatualizada.");
        }

        entity.ExcluidaEmUtc = _clock.GetUtcNow();
        entity.Versao++;
        await _dbContext.SaveChangesAsync(cancellationToken);
    }

    public async Task<IdeiaDetalheDto> AvaliarAsync(
        string id,
        IdeiaAvaliacaoRequestDto request,
        string gestorId,
        CancellationToken cancellationToken)
    {
        var entity = await _dbContext.Ideias.FirstOrDefaultAsync(i => i.Id == id, cancellationToken);
        if (entity is null || entity.ExcluidaEmUtc is not null)
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Ideia não encontrada.");
        }

        if (entity.Versao != request.Versao)
        {
            throw new BusinessException(409, "CONCORRENCIA", "A versão informada está desatualizada.");
        }

        if (!StatusIdeiaTransitionValidator.CanTransition(entity.Status, request.Status))
        {
            throw new BusinessException(409, "TRANSICAO_STATUS_INVALIDA", "Transição de status não permitida.");
        }

        var becomingApproved = request.Status == StatusIdeia.APROVADA && entity.Status != StatusIdeia.APROVADA;

        if (request.Status != entity.Status)
        {
            entity.Status = request.Status;
            entity.Versao++;
        }

        if (request.Prioridade is not null)
        {
            entity.Prioridade = request.Prioridade.Value;
        }

        entity.AtualizadoEmUtc = _clock.GetUtcNow();

        await using var tx = await _dbContext.Database.BeginTransactionAsync(cancellationToken);

        try
        {
            await _dbContext.SaveChangesAsync(cancellationToken);
        }
        catch (DbUpdateConcurrencyException)
        {
            throw new BusinessException(409, "CONCORRENCIA", "A versão informada está desatualizada.");
        }

        if (becomingApproved)
        {
            _dbContext.EventosPontuacao.Add(new EventoPontuacao
            {
                Id = Guid.NewGuid().ToString("N"),
                AutorId = entity.AutorId,
                IdeiaId = entity.Id,
                Tipo = TipoEventoPontuacao.PRIMEIRA_APROVACAO,
                Pontos = 30,
                OcorridoEmUtc = entity.AtualizadoEmUtc
            });
            await _dbContext.SaveChangesAsync(cancellationToken);
        }

        await tx.CommitAsync(cancellationToken);

        var nome = await _dbContext.Usuarios.AsNoTracking()
            .Where(u => u.Id == entity.AutorId)
            .Select(u => u.Nome)
            .FirstAsync(cancellationToken);

        return MapDetalhe(entity, nome);
    }

    private static bool CanAccess(Ideia entity, string usuarioId, PerfilUsuario perfil) =>
        perfil == PerfilUsuario.GESTOR || entity.AutorId == usuarioId;

    private static IdeiaResumoDto MapResumo(Ideia e, string autorNome) => new()
    {
        Id = e.Id,
        Titulo = e.Titulo,
        Area = e.Area,
        Status = e.Status,
        Prioridade = e.Prioridade,
        AutorNome = autorNome,
        EstrategiaId = e.EstrategiaId,
        CriadoEm = e.CriadoEmUtc
    };

    private static IdeiaDetalheDto MapDetalhe(Ideia e, string autorNome)
    {
        var resumo = MapResumo(e, autorNome);
        return new IdeiaDetalheDto
        {
            Id = resumo.Id,
            Titulo = resumo.Titulo,
            Area = resumo.Area,
            Status = resumo.Status,
            Prioridade = resumo.Prioridade,
            AutorNome = resumo.AutorNome,
            EstrategiaId = resumo.EstrategiaId,
            CriadoEm = resumo.CriadoEm,
            Descricao = e.Descricao,
            AutorId = e.AutorId,
            EstrategiaVersao = e.EstrategiaVersao,
            Versao = e.Versao,
            ExcluidaEm = e.ExcluidaEmUtc,
            AtualizadoEm = e.AtualizadoEmUtc
        };
    }
}
