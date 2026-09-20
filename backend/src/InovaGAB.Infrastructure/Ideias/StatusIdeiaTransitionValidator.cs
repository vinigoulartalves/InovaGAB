using InovaGAB.Domain.Ideias;

namespace InovaGAB.Infrastructure.Ideias;

public static class StatusIdeiaTransitionValidator
{
    private static readonly Dictionary<StatusIdeia, HashSet<StatusIdeia>> Allowed = new()
    {
        [StatusIdeia.ENVIADA] = [StatusIdeia.EM_ANALISE, StatusIdeia.APROVADA, StatusIdeia.REJEITADA],
        [StatusIdeia.EM_ANALISE] = [StatusIdeia.APROVADA, StatusIdeia.REJEITADA],
        [StatusIdeia.REJEITADA] = [StatusIdeia.EM_ANALISE],
        [StatusIdeia.APROVADA] = [],
        [StatusIdeia.VIROU_PROJETO] = []
    };

    public static bool CanTransition(StatusIdeia from, StatusIdeia to)
    {
        if (from == to)
        {
            return true;
        }

        return Allowed.TryGetValue(from, out var targets) && targets.Contains(to);
    }
}
