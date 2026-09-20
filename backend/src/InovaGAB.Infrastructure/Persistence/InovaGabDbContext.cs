using InovaGAB.Domain.Auth;
using InovaGAB.Domain.Probe;
using InovaGAB.Domain.Usuarios;
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

    public DbSet<Usuario> Usuarios => Set<Usuario>();

    public DbSet<RefreshToken> RefreshTokens => Set<RefreshToken>();

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

        modelBuilder.Entity<Usuario>(entity =>
        {
            entity.ToCollection("usuarios");
            entity.HasKey(e => e.Id);
            entity.Property(e => e.EmailNormalizado).IsRequired();
        });

        modelBuilder.Entity<RefreshToken>(entity =>
        {
            entity.ToCollection("refresh_tokens");
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Versao).IsConcurrencyToken();
        });
    }
}
