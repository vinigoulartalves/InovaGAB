using InovaGAB.Domain.Estrategias;
using InovaGAB.Domain.Ideias;
using InovaGAB.Domain.Pontuacao;
using InovaGAB.Domain.Projetos;
using InovaGAB.Domain.Usuarios;
using InovaGAB.Infrastructure.Auth;
using InovaGAB.Infrastructure.Configuration;
using InovaGAB.Infrastructure.Persistence;
using InovaGAB.Infrastructure.Time;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace InovaGAB.Infrastructure.Seed;

public sealed class DevDataSeeder
{
    public const string CampanhaVigenteDemo = "SEED_DEMO_VIGENTE";
    public const string CampanhaVencidaDemo = "SEED_DEMO_VENCIDA";
    public const string ProjetoDemoANome = "Seed Projeto A (demo)";
    public const string ProjetoDemoBNome = "Seed Projeto B (demo)";

    private readonly InovaGabDbContext _dbContext;
    private readonly PasswordHasher<Usuario> _passwordHasher;
    private readonly IConfiguration _configuration;
    private readonly SeedOptions _seedOptions;
    private readonly IVigenciaClock _clock;
    private readonly ILogger<DevDataSeeder> _logger;

    public DevDataSeeder(
        InovaGabDbContext dbContext,
        PasswordHasher<Usuario> passwordHasher,
        IConfiguration configuration,
        IOptions<SeedOptions> seedOptions,
        IVigenciaClock clock,
        ILogger<DevDataSeeder> logger)
    {
        _dbContext = dbContext;
        _passwordHasher = passwordHasher;
        _configuration = configuration;
        _seedOptions = seedOptions.Value;
        _clock = clock;
        _logger = logger;
    }

    public async Task SeedAsync(CancellationToken cancellationToken)
    {
        if (!_seedOptions.Enabled)
        {
            return;
        }

        await SeedUsuariosAsync(cancellationToken);
        await SeedDominioDemoAsync(cancellationToken);
    }

    private async Task SeedUsuariosAsync(CancellationToken cancellationToken)
    {
        var definitions = new[]
        {
            ("operador1@inovagab.local", "Operador Um", PerfilUsuario.OPERADOR, "DEV_PASSWORD_OPERADOR1"),
            ("operador2@inovagab.local", "Operador Dois", PerfilUsuario.OPERADOR, "DEV_PASSWORD_OPERADOR2"),
            ("gestor@inovagab.local", "Gestor Demo", PerfilUsuario.GESTOR, "DEV_PASSWORD_GESTOR"),
            ("lider@inovagab.local", "Líder Demo", PerfilUsuario.LIDER, "DEV_PASSWORD_LIDER")
        };

        var changed = false;

        foreach (var (email, nome, perfil, passwordKey) in definitions)
        {
            var normalizado = AuthService.NormalizeEmail(email);
            var senha = _configuration[passwordKey]?.Trim();
            if (string.IsNullOrWhiteSpace(senha))
            {
                _logger.LogWarning(
                    "Seed ignorado para {Email}: variável {PasswordKey} não definida.",
                    email,
                    passwordKey);
                continue;
            }

            var usuario = await _dbContext.Usuarios
                .FirstOrDefaultAsync(
                    u => u.EmailNormalizado == normalizado || u.Email == email,
                    cancellationToken);

            if (usuario is null)
            {
                usuario = new Usuario
                {
                    Id = Guid.NewGuid().ToString("N"),
                    Nome = nome,
                    Email = email,
                    EmailNormalizado = normalizado,
                    Perfil = perfil,
                    Ativo = true,
                    Demo = true,
                    CriadoEmUtc = DateTime.UtcNow
                };
                usuario.PasswordHash = _passwordHasher.HashPassword(usuario, senha);
                _dbContext.Usuarios.Add(usuario);
                _logger.LogInformation("Usuário demo criado: {Email} ({Perfil})", email, perfil);
                changed = true;
                continue;
            }

            if (string.IsNullOrWhiteSpace(usuario.EmailNormalizado))
            {
                usuario.EmailNormalizado = normalizado;
                changed = true;
            }

            if (!usuario.Demo)
            {
                continue;
            }

            var verify = _passwordHasher.VerifyHashedPassword(usuario, usuario.PasswordHash, senha);
            if (verify == PasswordVerificationResult.Failed)
            {
                usuario.PasswordHash = _passwordHasher.HashPassword(usuario, senha);
                _logger.LogInformation(
                    "Senha demo sincronizada ({PasswordKey}) para {Email}",
                    passwordKey,
                    email);
                changed = true;
            }
        }

        if (changed)
        {
            await _dbContext.SaveChangesAsync(cancellationToken);
        }
    }

    private async Task SeedDominioDemoAsync(CancellationToken cancellationToken)
    {
        if (await _dbContext.Projetos.AnyAsync(p => p.Demo && p.Nome == ProjetoDemoANome, cancellationToken))
        {
            return;
        }

        var op1 = await FindUserAsync("operador1@inovagab.local", cancellationToken);
        var op2 = await FindUserAsync("operador2@inovagab.local", cancellationToken);
        var gestor = await FindUserAsync("gestor@inovagab.local", cancellationToken);

        if (op1 is null || op2 is null || gestor is null)
        {
            _logger.LogWarning("Seed de domínio demo ignorado: usuários demo incompletos.");
            return;
        }

        var hoje = _clock.GetTodaySaoPaulo();
        var now = _clock.GetUtcNow();

        var vigente = await EnsureEstrategiaAsync(
            CampanhaVigenteDemo,
            "Estratégia demo vigente",
            hoje.AddMonths(-1),
            hoje.AddMonths(6),
            true,
            now,
            cancellationToken);

        var vencida = await EnsureEstrategiaAsync(
            CampanhaVencidaDemo,
            "Estratégia demo vencida",
            hoje.AddMonths(-12),
            hoje.AddDays(-30),
            false,
            now,
            cancellationToken);

        var ideiaEnviadaOp1 = CreateIdeia(op1.Id, vigente, StatusIdeia.ENVIADA, "Ideia enviada op1", now);
        var ideiaAnaliseOp2 = CreateIdeia(op2.Id, vigente, StatusIdeia.EM_ANALISE, "Ideia em análise op2", now);
        var ideiaAprovadaOp1 = CreateIdeia(op1.Id, vigente, StatusIdeia.APROVADA, "Ideia aprovada op1", now);
        var ideiaRejeitadaOp2 = CreateIdeia(op2.Id, vigente, StatusIdeia.REJEITADA, "Ideia rejeitada op2", now);
        var ideiaConvertida = CreateIdeia(op2.Id, vencida, StatusIdeia.VIROU_PROJETO, "Ideia convertida op2", now);

        _dbContext.Ideias.AddRange(
            ideiaEnviadaOp1,
            ideiaAnaliseOp2,
            ideiaAprovadaOp1,
            ideiaRejeitadaOp2,
            ideiaConvertida);

        var projetoA = new Projeto
        {
            Id = Guid.NewGuid().ToString("N"),
            Nome = ProjetoDemoANome,
            Descricao = "Cenário relatório A",
            EstrategiaId = vigente.Id,
            EstrategiaVersao = vigente.Versao,
            ResponsavelId = gestor.Id,
            Etapa = "Execução",
            Status = StatusProjeto.EM_ANDAMENTO,
            Investimento = 1000m,
            RetornoFinanceiro = 1500m,
            ReducaoCustos = 0m,
            GanhoProdutividade = 10m,
            Prazo = hoje.AddMonths(3),
            Versao = 1,
            Demo = true,
            CriadoEmUtc = now,
            AtualizadoEmUtc = now
        };

        var projetoB = new Projeto
        {
            Id = Guid.NewGuid().ToString("N"),
            Nome = ProjetoDemoBNome,
            Descricao = "Cenário relatório B",
            EstrategiaId = vigente.Id,
            EstrategiaVersao = vigente.Versao,
            ResponsavelId = gestor.Id,
            Etapa = "Execução",
            Status = StatusProjeto.EM_ANDAMENTO,
            Investimento = 2000m,
            RetornoFinanceiro = 2600m,
            ReducaoCustos = 0m,
            GanhoProdutividade = 20m,
            Prazo = hoje.AddMonths(3),
            Versao = 1,
            Demo = true,
            CriadoEmUtc = now,
            AtualizadoEmUtc = now
        };

        var projetoConvertido = new Projeto
        {
            Id = Guid.NewGuid().ToString("N"),
            Nome = "Projeto da ideia convertida",
            Descricao = "Derivado de ideia",
            IdeiaId = ideiaConvertida.Id,
            EstrategiaId = vencida.Id,
            EstrategiaVersao = vencida.Versao,
            ResponsavelId = gestor.Id,
            Etapa = "Planejamento",
            Status = StatusProjeto.PLANEJADO,
            Investimento = 500m,
            RetornoFinanceiro = 600m,
            ReducaoCustos = 0m,
            GanhoProdutividade = 5m,
            Prazo = hoje.AddMonths(2),
            Versao = 1,
            Demo = true,
            CriadoEmUtc = now,
            AtualizadoEmUtc = now
        };

        _dbContext.Projetos.AddRange(projetoA, projetoB, projetoConvertido);

        _dbContext.EventosPontuacao.AddRange(
            Evento(op1.Id, ideiaEnviadaOp1.Id, TipoEventoPontuacao.CADASTRO, 10, now),
            Evento(op1.Id, ideiaAprovadaOp1.Id, TipoEventoPontuacao.CADASTRO, 10, now),
            Evento(op1.Id, ideiaAprovadaOp1.Id, TipoEventoPontuacao.PRIMEIRA_APROVACAO, 30, now),
            Evento(op2.Id, ideiaAnaliseOp2.Id, TipoEventoPontuacao.CADASTRO, 10, now),
            Evento(op2.Id, ideiaConvertida.Id, TipoEventoPontuacao.CADASTRO, 10, now));

        await _dbContext.SaveChangesAsync(cancellationToken);
        _logger.LogInformation("Seed demo de estratégias, ideias e projetos aplicado.");
    }

    private async Task<Estrategia> EnsureEstrategiaAsync(
        string campanha,
        string titulo,
        DateOnly inicio,
        DateOnly? fim,
        bool ativa,
        DateTime now,
        CancellationToken cancellationToken)
    {
        var existing = await _dbContext.Estrategias
            .FirstOrDefaultAsync(e => e.Campanha == campanha && e.ExcluidaEmUtc == null, cancellationToken);

        if (existing is not null)
        {
            return existing;
        }

        var estrategia = new Estrategia
        {
            Id = Guid.NewGuid().ToString("N"),
            Titulo = titulo,
            Descricao = "Dados de demonstração Sprint 2",
            Categoria = "Demo",
            Campanha = campanha,
            InicioVigencia = inicio,
            FimVigencia = fim,
            Ativa = ativa,
            Versao = 1,
            CriadoEmUtc = now,
            AtualizadoEmUtc = now
        };

        _dbContext.Estrategias.Add(estrategia);
        await _dbContext.SaveChangesAsync(cancellationToken);
        return estrategia;
    }

    private async Task<Usuario?> FindUserAsync(string email, CancellationToken cancellationToken)
    {
        var norm = AuthService.NormalizeEmail(email);
        return await _dbContext.Usuarios.AsNoTracking()
            .FirstOrDefaultAsync(u => u.EmailNormalizado == norm, cancellationToken);
    }

    private static Ideia CreateIdeia(
        string autorId,
        Estrategia estrategia,
        StatusIdeia status,
        string titulo,
        DateTime now) =>
        new()
        {
            Id = Guid.NewGuid().ToString("N"),
            Titulo = titulo,
            Descricao = "Seed demo",
            Area = "Operações",
            AutorId = autorId,
            Status = status,
            Prioridade = PrioridadeIdeia.MEDIA,
            EstrategiaId = estrategia.Id,
            EstrategiaVersao = estrategia.Versao,
            Versao = 1,
            CriadoEmUtc = now,
            AtualizadoEmUtc = now
        };

    private static EventoPontuacao Evento(
        string autorId,
        string ideiaId,
        TipoEventoPontuacao tipo,
        int pontos,
        DateTime now) =>
        new()
        {
            Id = Guid.NewGuid().ToString("N"),
            AutorId = autorId,
            IdeiaId = ideiaId,
            Tipo = tipo,
            Pontos = pontos,
            OcorridoEmUtc = now
        };
}
