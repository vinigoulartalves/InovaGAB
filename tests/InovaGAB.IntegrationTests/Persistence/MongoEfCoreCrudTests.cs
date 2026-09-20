using InovaGAB.Domain.Probe;
using InovaGAB.Infrastructure.Persistence;
using InovaGAB.IntegrationTests.Infrastructure;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Xunit;

namespace InovaGAB.IntegrationTests.Persistence;

public sealed class MongoEfCoreCrudTests
{
    [Fact]
    public async Task EfCore_crud_decimal_enum_dates_concurrency_and_transaction()
    {
        MongoTestEnvironment.EnsureAvailable();

        var services = new ServiceCollection();
        services.AddLogging();
        services.AddDbContext<InovaGabDbContext>(options =>
        {
            options.UseMongoDB(MongoTestEnvironment.ConnectionString!, MongoTestEnvironment.DatabaseName);
        });

        await using var provider = services.BuildServiceProvider();
        await using var scope = provider.CreateAsyncScope();
        var db = scope.ServiceProvider.GetRequiredService<InovaGabDbContext>();

        await db.Database.EnsureCreatedAsync();

        var id = Guid.NewGuid().ToString("N");
        var entity = new IntegrationProbeDocument
        {
            Id = id,
            Name = "probe-ef",
            Amount = 123.4567m,
            Status = ProbeDocumentStatus.Active,
            BusinessDate = new DateOnly(2026, 9, 20),
            CreatedAtUtc = DateTime.UtcNow,
            Versao = 1
        };

        db.IntegrationProbes.Add(entity);
        await db.SaveChangesAsync();

        var loaded = await db.IntegrationProbes.AsNoTracking().FirstAsync(x => x.Id == id);
        Assert.Equal(123.4567m, loaded.Amount);
        Assert.Equal(ProbeDocumentStatus.Active, loaded.Status);
        Assert.Equal(new DateOnly(2026, 9, 20), loaded.BusinessDate);

        var tracked = await db.IntegrationProbes.FirstAsync(x => x.Id == id);
        tracked.Amount = 200.5m;
        tracked.Versao = 2;
        await db.SaveChangesAsync();

        db.ChangeTracker.Clear();
        var staleAttach = new IntegrationProbeDocument
        {
            Id = id,
            Name = "probe-ef",
            Amount = 200.5m,
            Status = ProbeDocumentStatus.Active,
            BusinessDate = new DateOnly(2026, 9, 20),
            CreatedAtUtc = loaded.CreatedAtUtc,
            Versao = 1
        };
        db.IntegrationProbes.Attach(staleAttach);
        staleAttach.Name = "conflict";
        await Assert.ThrowsAsync<DbUpdateConcurrencyException>(() => db.SaveChangesAsync());

        var fresh = await db.IntegrationProbes.FirstAsync(x => x.Id == id);
        fresh.Name = "updated";
        fresh.Versao = 3;
        await db.SaveChangesAsync();

        await using (var transaction = await db.Database.BeginTransactionAsync())
        {
            var txEntity = new IntegrationProbeDocument
            {
                Id = Guid.NewGuid().ToString("N"),
                Name = "tx-probe",
                Amount = 1m,
                Status = ProbeDocumentStatus.Draft,
                BusinessDate = DateOnly.FromDateTime(DateTime.UtcNow),
                CreatedAtUtc = DateTime.UtcNow,
                Versao = 1
            };
            db.IntegrationProbes.Add(txEntity);
            await db.SaveChangesAsync();
            await transaction.CommitAsync();
        }

        db.IntegrationProbes.Remove(fresh);
        await db.SaveChangesAsync();

        var exists = await db.IntegrationProbes.AnyAsync(x => x.Id == id);
        Assert.False(exists);
    }
}
