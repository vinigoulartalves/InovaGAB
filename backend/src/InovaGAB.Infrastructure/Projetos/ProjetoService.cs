using InovaGAB.Application.Common;
using InovaGAB.Application.Exceptions;
using InovaGAB.Application.Projetos;
using InovaGAB.Application.Projetos.Dtos;
using InovaGAB.Domain.Ideias;
using InovaGAB.Domain.Projetos;
using InovaGAB.Domain.Usuarios;
using InovaGAB.Infrastructure.Common;
using InovaGAB.Infrastructure.Estrategias;
using InovaGAB.Infrastructure.Persistence;
using InovaGAB.Infrastructure.Time;
using Microsoft.EntityFrameworkCore;
using MongoDB.Driver;

namespace InovaGAB.Infrastructure.Projetos;

public sealed class ProjetoService : IProjetoService
{
    private readonly InovaGabDbContext _dbContext;
    private readonly VigenciaEvaluator _vigencia;
    private readonly IVigenciaClock _clock;

    public ProjetoService(
        InovaGabDbContext dbContext,
        VigenciaEvaluator vigencia,
        IVigenciaClock clock)
    {
        _dbContext = dbContext;
        _vigencia = vigencia;
        _clock = clock;
    }

    public async Task<PagedResultDto<ProjetoResumoDto>> ListAsync(
        ProjetoListQuery query,
        PerfilUsuario perfil,
        CancellationToken cancellationToken)
    {
        EnsureCanRead(perfil);

        var (page, pageSize) = QueryPaging.Normalize(query.Page, query.PageSize);
        var q = _dbContext.Projetos.AsNoTracking().Where(p => p.ExcluidaEmUtc == null);

        if (query.Status is not null)
        {
            q = q.Where(p => p.Status == query.Status);
        }

        if (!string.IsNullOrWhiteSpace(query.EstrategiaId))
        {
            q = q.Where(p => p.EstrategiaId == query.EstrategiaId);
        }

        if (!string.IsNullOrWhiteSpace(query.ResponsavelId))
        {
            q = q.Where(p => p.ResponsavelId == query.ResponsavelId);
        }

        if (query.Inicio is not null)
        {
            var start = query.Inicio.Value.ToDateTime(TimeOnly.MinValue, DateTimeKind.Utc);
            q = q.Where(p => p.CriadoEmUtc >= start);
        }

        if (query.Fim is not null)
        {
            var end = query.Fim.Value.AddDays(1).ToDateTime(TimeOnly.MinValue, DateTimeKind.Utc);
            q = q.Where(p => p.CriadoEmUtc < end);
        }

        var total = await q.CountAsync(cancellationToken);
        var items = await q
            .OrderByDescending(p => p.CriadoEmUtc)
            .ThenBy(p => p.Id)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync(cancellationToken);

        return new PagedResultDto<ProjetoResumoDto>
        {
            Items = items.Select(ProjetoMapper.ToResumo).ToList(),
            Page = page,
            PageSize = pageSize,
            TotalItems = total,
            TotalPages = (int)Math.Ceiling(total / (double)pageSize)
        };
    }

    public async Task<ProjetoDetalheDto> GetAsync(
        string id,
        PerfilUsuario perfil,
        CancellationToken cancellationToken)
    {
        EnsureCanRead(perfil);

        var entity = await _dbContext.Projetos.AsNoTracking()
            .FirstOrDefaultAsync(p => p.Id == id, cancellationToken);

        if (entity is null || entity.ExcluidaEmUtc is not null)
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Projeto não encontrado.");
        }

        var responsavelNome = await _dbContext.Usuarios.AsNoTracking()
            .Where(u => u.Id == entity.ResponsavelId)
            .Select(u => u.Nome)
            .FirstOrDefaultAsync(cancellationToken) ?? "";

        return ProjetoMapper.ToDetalhe(entity, responsavelNome);
    }

    public async Task<ProjetoDetalheDto> CreateAsync(
        ProjetoCreateRequestDto request,
        CancellationToken cancellationToken)
    {
        ValidateProjetoFields(request.Nome, request.Descricao, request.Investimento, request.RetornoFinanceiro,
            request.ReducaoCustos, request.GanhoProdutividade);

        var estrategia = await LoadEstrategiaAsync(request.EstrategiaId, cancellationToken);
        if (!_vigencia.IsVigente(estrategia))
        {
            throw new BusinessException(409, "ESTRATEGIA_NAO_VIGENTE", "Estratégia não está vigente para novos vínculos.");
        }

        await EnsureGestorResponsavelAtivoAsync(request.ResponsavelId, cancellationToken);

        var now = _clock.GetUtcNow();
        var projeto = new Projeto
        {
            Id = Guid.NewGuid().ToString("N"),
            Nome = request.Nome.Trim(),
            Descricao = request.Descricao.Trim(),
            IdeiaId = null,
            EstrategiaId = estrategia.Id,
            EstrategiaVersao = estrategia.Versao,
            ResponsavelId = request.ResponsavelId,
            Etapa = request.Etapa.Trim(),
            Status = request.Status,
            Investimento = request.Investimento,
            RetornoFinanceiro = request.RetornoFinanceiro,
            ReducaoCustos = request.ReducaoCustos,
            GanhoProdutividade = request.GanhoProdutividade,
            Prazo = request.Prazo,
            Versao = 1,
            CriadoEmUtc = now,
            AtualizadoEmUtc = now
        };

        _dbContext.Projetos.Add(projeto);
        await _dbContext.SaveChangesAsync(cancellationToken);

        var nome = await GetResponsavelNomeAsync(projeto.ResponsavelId, cancellationToken);
        return ProjetoMapper.ToDetalhe(projeto, nome);
    }

    public async Task<ProjetoDetalheDto> UpdateAsync(
        string id,
        ProjetoUpdateRequestDto request,
        CancellationToken cancellationToken)
    {
        ValidateProjetoFields(request.Nome, request.Descricao, request.Investimento, request.RetornoFinanceiro,
            request.ReducaoCustos, request.GanhoProdutividade);

        var entity = await _dbContext.Projetos.FirstOrDefaultAsync(p => p.Id == id, cancellationToken);
        if (entity is null || entity.ExcluidaEmUtc is not null)
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Projeto não encontrado.");
        }

        if (entity.Versao != request.Versao)
        {
            throw new BusinessException(409, "CONCORRENCIA", "A versão informada está desatualizada.");
        }

        await EnsureGestorResponsavelAtivoAsync(request.ResponsavelId, cancellationToken);

        entity.Nome = request.Nome.Trim();
        entity.Descricao = request.Descricao.Trim();
        entity.ResponsavelId = request.ResponsavelId;
        entity.Etapa = request.Etapa.Trim();
        entity.Status = request.Status;
        entity.Investimento = request.Investimento;
        entity.RetornoFinanceiro = request.RetornoFinanceiro;
        entity.ReducaoCustos = request.ReducaoCustos;
        entity.GanhoProdutividade = request.GanhoProdutividade;
        entity.Prazo = request.Prazo;
        entity.Versao++;
        entity.AtualizadoEmUtc = _clock.GetUtcNow();

        try
        {
            await _dbContext.SaveChangesAsync(cancellationToken);
        }
        catch (DbUpdateConcurrencyException)
        {
            throw new BusinessException(409, "CONCORRENCIA", "A versão informada está desatualizada.");
        }

        var nome = await GetResponsavelNomeAsync(entity.ResponsavelId, cancellationToken);
        return ProjetoMapper.ToDetalhe(entity, nome);
    }

    public async Task DeleteLogicalAsync(string id, int versaoEsperada, CancellationToken cancellationToken)
    {
        var entity = await _dbContext.Projetos.FirstOrDefaultAsync(p => p.Id == id, cancellationToken);
        if (entity is null || entity.ExcluidaEmUtc is not null)
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Projeto não encontrado.");
        }

        if (entity.Versao != versaoEsperada)
        {
            throw new BusinessException(409, "CONCORRENCIA", "A versão informada está desatualizada.");
        }

        entity.ExcluidaEmUtc = _clock.GetUtcNow();
        entity.Versao++;
        await _dbContext.SaveChangesAsync(cancellationToken);
    }

    public async Task<ProjetoDetalheDto> ConverterIdeiaAsync(
        string ideiaId,
        ConversaoIdeiaProjetoRequestDto request,
        CancellationToken cancellationToken)
    {
        ValidateProjetoFields(request.Nome, request.Descricao, request.Investimento, request.RetornoFinanceiro,
            request.ReducaoCustos, request.GanhoProdutividade);

        var ideia = await _dbContext.Ideias.FirstOrDefaultAsync(i => i.Id == ideiaId, cancellationToken);
        if (ideia is null || ideia.ExcluidaEmUtc is not null)
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Ideia não encontrada.");
        }

        if (ideia.Versao != request.Versao)
        {
            throw new BusinessException(409, "CONCORRENCIA", "A versão informada está desatualizada.");
        }

        if (ideia.Status == StatusIdeia.VIROU_PROJETO)
        {
            throw new BusinessException(409, "CONVERSAO_DUPLICADA", "Esta ideia já foi convertida em projeto.");
        }

        if (ideia.Status != StatusIdeia.APROVADA)
        {
            throw new BusinessException(409, "STATUS_INVALIDO", "Apenas ideias aprovadas podem ser convertidas.");
        }

        var projetoExistente = await _dbContext.Projetos.AsNoTracking()
            .AnyAsync(p => p.IdeiaId == ideiaId, cancellationToken);
        if (projetoExistente)
        {
            throw new BusinessException(409, "CONVERSAO_DUPLICADA", "Já existe projeto vinculado a esta ideia.");
        }

        var estrategia = await LoadEstrategiaAsync(ideia.EstrategiaId, cancellationToken);
        if (!_vigencia.IsVigente(estrategia))
        {
            throw new BusinessException(409, "ESTRATEGIA_NAO_VIGENTE", "Estratégia não está vigente para conversão.");
        }

        await EnsureGestorResponsavelAtivoAsync(request.ResponsavelId, cancellationToken);

        var now = _clock.GetUtcNow();
        var projeto = new Projeto
        {
            Id = Guid.NewGuid().ToString("N"),
            Nome = request.Nome.Trim(),
            Descricao = request.Descricao.Trim(),
            IdeiaId = ideia.Id,
            EstrategiaId = ideia.EstrategiaId,
            EstrategiaVersao = ideia.EstrategiaVersao,
            ResponsavelId = request.ResponsavelId,
            Etapa = request.Etapa.Trim(),
            Status = request.Status,
            Investimento = request.Investimento,
            RetornoFinanceiro = request.RetornoFinanceiro,
            ReducaoCustos = request.ReducaoCustos,
            GanhoProdutividade = request.GanhoProdutividade,
            Prazo = request.Prazo,
            Versao = 1,
            CriadoEmUtc = now,
            AtualizadoEmUtc = now
        };

        await using var tx = await _dbContext.Database.BeginTransactionAsync(cancellationToken);

        ideia.Status = StatusIdeia.VIROU_PROJETO;
        ideia.Versao++;
        ideia.AtualizadoEmUtc = now;

        _dbContext.Projetos.Add(projeto);

        try
        {
            await _dbContext.SaveChangesAsync(cancellationToken);
            await tx.CommitAsync(cancellationToken);
        }
        catch (Exception ex) when (IsDuplicateIdeiaIndex(ex))
        {
            await tx.RollbackAsync(cancellationToken);
            throw new BusinessException(409, "CONVERSAO_DUPLICADA", "Já existe projeto vinculado a esta ideia.");
        }
        catch (DbUpdateConcurrencyException)
        {
            await tx.RollbackAsync(cancellationToken);
            throw new BusinessException(409, "CONCORRENCIA", "A versão informada está desatualizada.");
        }
        catch
        {
            await tx.RollbackAsync(cancellationToken);
            throw;
        }

        var nome = await GetResponsavelNomeAsync(projeto.ResponsavelId, cancellationToken);
        return ProjetoMapper.ToDetalhe(projeto, nome);
    }

    private static bool IsDuplicateIdeiaIndex(Exception ex)
    {
        if (ex is MongoWriteException mwe && mwe.WriteError.Category == ServerErrorCategory.DuplicateKey)
        {
            return true;
        }

        return ex.InnerException is not null && IsDuplicateIdeiaIndex(ex.InnerException);
    }

    private static void EnsureCanRead(PerfilUsuario perfil)
    {
        if (perfil == PerfilUsuario.OPERADOR)
        {
            throw new BusinessException(403, "ACESSO_NEGADO", "Perfil sem permissão para projetos.");
        }
    }

    private static void ValidateProjetoFields(
        string nome,
        string descricao,
        decimal investimento,
        decimal retorno,
        decimal reducaoCustos,
        decimal ganhoProdutividade)
    {
        if (string.IsNullOrWhiteSpace(nome) || string.IsNullOrWhiteSpace(descricao))
        {
            throw new BusinessException(400, "VALIDACAO", "Nome e descrição são obrigatórios.");
        }

        if (investimento < 0 || retorno < 0 || reducaoCustos < 0 || ganhoProdutividade < 0)
        {
            throw new BusinessException(400, "VALIDACAO", "Valores financeiros não podem ser negativos.");
        }
    }

    private async Task<Domain.Estrategias.Estrategia> LoadEstrategiaAsync(
        string estrategiaId,
        CancellationToken cancellationToken)
    {
        var estrategia = await _dbContext.Estrategias.AsNoTracking()
            .FirstOrDefaultAsync(e => e.Id == estrategiaId, cancellationToken);

        if (estrategia is null || estrategia.ExcluidaEmUtc is not null)
        {
            throw new BusinessException(404, "NAO_ENCONTRADO", "Estratégia não encontrada.");
        }

        return estrategia;
    }

    private async Task EnsureGestorResponsavelAtivoAsync(string responsavelId, CancellationToken cancellationToken)
    {
        var ok = await _dbContext.Usuarios.AsNoTracking()
            .AnyAsync(
                u => u.Id == responsavelId
                    && u.Perfil == PerfilUsuario.GESTOR
                    && u.Ativo,
                cancellationToken);

        if (!ok)
        {
            throw new BusinessException(400, "VALIDACAO", "Responsável deve ser um gestor ativo.");
        }
    }

    private async Task<string> GetResponsavelNomeAsync(string responsavelId, CancellationToken cancellationToken) =>
        await _dbContext.Usuarios.AsNoTracking()
            .Where(u => u.Id == responsavelId)
            .Select(u => u.Nome)
            .FirstOrDefaultAsync(cancellationToken) ?? "";
}
