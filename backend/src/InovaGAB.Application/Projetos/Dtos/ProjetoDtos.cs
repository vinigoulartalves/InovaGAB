using InovaGAB.Domain.Projetos;

namespace InovaGAB.Application.Projetos.Dtos;

public sealed class ProjetoListQuery
{
    public StatusProjeto? Status { get; set; }

    public string? EstrategiaId { get; set; }

    public string? ResponsavelId { get; set; }

    public DateOnly? Inicio { get; set; }

    public DateOnly? Fim { get; set; }

    public int Page { get; set; } = 1;

    public int PageSize { get; set; } = 20;
}

public class ProjetoResumoDto
{
    public string Id { get; set; } = string.Empty;

    public string Nome { get; set; } = string.Empty;

    public StatusProjeto Status { get; set; }

    public string EstrategiaId { get; set; } = string.Empty;

    public string ResponsavelId { get; set; } = string.Empty;

    public decimal Investimento { get; set; }

    public decimal RetornoFinanceiro { get; set; }

    public DateTime CriadoEm { get; set; }
}

public sealed class ProjetoDetalheDto : ProjetoResumoDto
{
    public string Descricao { get; set; } = string.Empty;

    public string? IdeiaId { get; set; }

    public int EstrategiaVersao { get; set; }

    public string ResponsavelNome { get; set; } = string.Empty;

    public string Etapa { get; set; } = string.Empty;

    public decimal ReducaoCustos { get; set; }

    public decimal GanhoProdutividade { get; set; }

    public DateOnly Prazo { get; set; }

    public int Versao { get; set; }

    public DateTime? ExcluidaEm { get; set; }

    public DateTime AtualizadoEm { get; set; }
}

public sealed class ProjetoCreateRequestDto
{
    /// <summary>Enviado apenas para rejeição na criação direta; não é persistido.</summary>
    public string? IdeiaId { get; set; }

    public string Nome { get; set; } = string.Empty;

    public string Descricao { get; set; } = string.Empty;

    public string EstrategiaId { get; set; } = string.Empty;

    public string ResponsavelId { get; set; } = string.Empty;

    public string Etapa { get; set; } = string.Empty;

    public StatusProjeto Status { get; set; } = StatusProjeto.PLANEJADO;

    public decimal Investimento { get; set; }

    public decimal RetornoFinanceiro { get; set; }

    public decimal ReducaoCustos { get; set; }

    public decimal GanhoProdutividade { get; set; }

    public DateOnly Prazo { get; set; }
}

public sealed class ProjetoUpdateRequestDto
{
    public int Versao { get; set; }

    public string Nome { get; set; } = string.Empty;

    public string Descricao { get; set; } = string.Empty;

    public string ResponsavelId { get; set; } = string.Empty;

    public string Etapa { get; set; } = string.Empty;

    public StatusProjeto Status { get; set; }

    public decimal Investimento { get; set; }

    public decimal RetornoFinanceiro { get; set; }

    public decimal ReducaoCustos { get; set; }

    public decimal GanhoProdutividade { get; set; }

    public DateOnly Prazo { get; set; }
}

public sealed class ConversaoIdeiaProjetoRequestDto
{
    public int Versao { get; set; }

    public string Nome { get; set; } = string.Empty;

    public string Descricao { get; set; } = string.Empty;

    public string ResponsavelId { get; set; } = string.Empty;

    public string Etapa { get; set; } = string.Empty;

    public StatusProjeto Status { get; set; } = StatusProjeto.PLANEJADO;

    public decimal Investimento { get; set; }

    public decimal RetornoFinanceiro { get; set; }

    public decimal ReducaoCustos { get; set; }

    public decimal GanhoProdutividade { get; set; }

    public DateOnly Prazo { get; set; }
}
