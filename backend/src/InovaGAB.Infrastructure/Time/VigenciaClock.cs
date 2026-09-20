namespace InovaGAB.Infrastructure.Time;

public sealed class VigenciaClock : IVigenciaClock
{
    private static readonly TimeZoneInfo SaoPaulo = TimeZoneInfo.FindSystemTimeZoneById("America/Sao_Paulo");
    private readonly TimeProvider _timeProvider;

    public VigenciaClock(TimeProvider timeProvider) => _timeProvider = timeProvider;

    public DateOnly GetTodaySaoPaulo()
    {
        var utc = _timeProvider.GetUtcNow().UtcDateTime;
        var local = TimeZoneInfo.ConvertTimeFromUtc(utc, SaoPaulo);
        return DateOnly.FromDateTime(local);
    }

    public DateTime GetUtcNow() => _timeProvider.GetUtcNow().UtcDateTime;
}
