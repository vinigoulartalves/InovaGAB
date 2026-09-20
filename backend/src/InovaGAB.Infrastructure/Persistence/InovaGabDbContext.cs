using InovaGAB.Domain.Auth;
using InovaGAB.Domain.Estrategias;
using InovaGAB.Domain.Ideias;
using InovaGAB.Domain.Pontuacao;
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

    public DbSet<Estrategia> Estrategias => Set<Estrategia>();

    public DbSet<EstrategiaHistorico> EstrategiasHistorico => Set<EstrategiaHistorico>();

    public DbSet<Ideia> Ideias => Set<Ideia>();

    public DbSet<EventoPontuacao> EventosPontuacao => Set<EventoPontuacao>();

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

        modelBuilder.Entity<Estrategia>(entity =>
        {
            entity.ToCollection("estrategias");
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Versao).IsConcurrencyToken();
        });

        modelBuilder.Entity<EstrategiaHistorico>(entity =>
        {
            entity.ToCollection("estrategias_historico");
            entity.HasKey(e => e.Id);
            entity.OwnsOne(e => e.Snapshot);
        });

        modelBuilder.Entity<Ideia>(entity =>
        {
            entity.ToCollection("ideias");
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Versao).IsConcurrencyToken();
        });

        modelBuilder.Entity<EventoPontuacao>(entity =>
        {
            entity.ToCollection("eventos_pontuacao");
            entity.HasKey(e => e.Id);
        });
    }
}
