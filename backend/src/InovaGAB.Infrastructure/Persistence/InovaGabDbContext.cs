using InovaGAB.Domain.Probe;
using Microsoft.EntityFrameworkCore;
using MongoDB.EntityFrameworkCore.Extensions;

namespace InovaGAB.Infrastructure.Persistence;

public sealed class InovaGabDbContext : DbContext
{
    public InovaGabDbContext(DbContextOptions<InovaGabDbContext> options)
        : base(options)
    {
    }

    public DbSet<IntegrationProbeDocument> IntegrationProbes => Set<IntegrationProbeDocument>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<IntegrationProbeDocument>(entity =>
        {
            entity.ToCollection("integration_probes");
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Amount).HasPrecision(18, 4);
            entity.Property(e => e.Versao).IsConcurrencyToken();
        });
    }
}
