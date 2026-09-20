namespace InovaGAB.Infrastructure.Time;

/// <summary>
/// Relógio injetável. Datas de vigência usam o calendário civil em America/Sao_Paulo (documentado no README).
/// </summary>
public interface IVigenciaClock
{
    DateOnly GetTodaySaoPaulo();

    DateTime GetUtcNow();
}
