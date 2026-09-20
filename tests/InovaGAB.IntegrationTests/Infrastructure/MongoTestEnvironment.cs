namespace InovaGAB.IntegrationTests.Infrastructure;

public static class MongoTestEnvironment
{
    public static string? ConnectionString =>
        Environment.GetEnvironmentVariable("MONGODB_URI")
        ?? Environment.GetEnvironmentVariable("Mongo__ConnectionString");

    public static string DatabaseName =>
        Environment.GetEnvironmentVariable("Mongo__DatabaseName") ?? "inovagab_test";

    public static bool IsAvailable => !string.IsNullOrWhiteSpace(ConnectionString);

    public static void EnsureAvailable()
    {
        if (!IsAvailable)
        {
            throw new InvalidOperationException(
                "MongoDB de teste indisponível. Defina MONGODB_URI ou execute via docker compose --profile tests.");
        }
    }
}
