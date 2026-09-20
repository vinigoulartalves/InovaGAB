using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;

namespace InovaGAB.Infrastructure.Persistence;

public sealed class MongoStartupInitializer
{
    private readonly InovaGabDbContext _dbContext;
    private readonly MongoIndexInitializer _indexInitializer;
    private readonly ILogger<MongoStartupInitializer> _logger;

    public MongoStartupInitializer(
        InovaGabDbContext dbContext,
        MongoIndexInitializer indexInitializer,
        ILogger<MongoStartupInitializer> logger)
    {
        _dbContext = dbContext;
        _indexInitializer = indexInitializer;
        _logger = logger;
    }

    public async Task InitializeAsync(CancellationToken cancellationToken)
    {
        await _dbContext.Database.EnsureCreatedAsync(cancellationToken);
        await _indexInitializer.InitializeAsync(cancellationToken);
        _logger.LogInformation("MongoDB startup initialization completed.");
    }
}
