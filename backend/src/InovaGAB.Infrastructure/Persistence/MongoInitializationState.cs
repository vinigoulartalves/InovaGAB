namespace InovaGAB.Infrastructure.Persistence;

public sealed class MongoInitializationState
{
    private volatile bool _isReady;

    public bool IsReady => _isReady;

    public void MarkReady() => _isReady = true;
}
