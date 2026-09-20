using InovaGAB.Domain.Estrategias;
using InovaGAB.Infrastructure.Time;

namespace InovaGAB.Infrastructure.Estrategias;

public sealed class VigenciaEvaluator
{
    private readonly IVigenciaClock _clock;

    public VigenciaEvaluator(IVigenciaClock clock) => _clock = clock;

    public bool IsVigente(Estrategia estrategia, DateOnly? referenceDate = null)
    {
        if (estrategia.ExcluidaEmUtc is not null)
        {
            return false;
        }

        if (!estrategia.Ativa)
        {
            return false;
        }

        var hoje = referenceDate ?? _clock.GetTodaySaoPaulo();
        if (estrategia.InicioVigencia > hoje)
        {
            return false;
        }

        if (estrategia.FimVigencia is not null && estrategia.FimVigencia < hoje)
        {
            return false;
        }

        return true;
    }
}
