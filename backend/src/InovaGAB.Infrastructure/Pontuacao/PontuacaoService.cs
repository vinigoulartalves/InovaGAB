using InovaGAB.Domain.Pontuacao;
using InovaGAB.Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;

namespace InovaGAB.Infrastructure.Pontuacao;

public sealed class PontuacaoService
{
    private readonly InovaGabDbContext _dbContext;
    private readonly ILogger<PontuacaoService> _logger;

    public PontuacaoService(InovaGabDbContext dbContext, ILogger<PontuacaoService> logger)
    {
        _dbContext = dbContext;
        _logger = logger;
    }

    public async Task TryRegistrarAsync(
        string autorId,
        string ideiaId,
        TipoEventoPontuacao tipo,
        int pontos,
        DateTime ocorridoEmUtc,
        CancellationToken cancellationToken)
    {
        var exists = await _dbContext.EventosPontuacao.AnyAsync(
            e => e.AutorId == autorId && e.IdeiaId == ideiaId && e.Tipo == tipo,
            cancellationToken);

        if (exists)
        {
            return;
        }

        _dbContext.EventosPontuacao.Add(new EventoPontuacao
        {
            Id = Guid.NewGuid().ToString("N"),
            AutorId = autorId,
            IdeiaId = ideiaId,
            Tipo = tipo,
            Pontos = pontos,
            OcorridoEmUtc = ocorridoEmUtc
        });

        try
        {
            await _dbContext.SaveChangesAsync(cancellationToken);
        }
        catch (DbUpdateException ex)
        {
            _logger.LogWarning(ex, "Evento de pontuação duplicado ignorado ({Tipo}, ideia {IdeiaId})", tipo, ideiaId);
        }
    }
}
