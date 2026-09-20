namespace InovaGAB.Domain.Projetos;

public sealed class Projeto
{
    public string Id { get; set; } = string.Empty;

    public string Nome { get; set; } = string.Empty;

    public string Descricao { get; set; } = string.Empty;

    public string? IdeiaId { get; set; }

    public string EstrategiaId { get; set; } = string.Empty;

    public int EstrategiaVersao { get; set; }

    public string ResponsavelId { get; set; } = string.Empty;

    public string Etapa { get; set; } = string.Empty;

    public StatusProjeto Status { get; set; } = StatusProjeto.PLANEJADO;

    public decimal Investimento { get; set; }

    public decimal RetornoFinanceiro { get; set; }

    public decimal ReducaoCustos { get; set; }

    public decimal GanhoProdutividade { get; set; }

    public DateOnly Prazo { get; set; }

    public int Versao { get; set; } = 1;

    public DateTime? ExcluidaEmUtc { get; set; }

    public DateTime CriadoEmUtc { get; set; }

    public DateTime AtualizadoEmUtc { get; set; }

    public bool Demo { get; set; }
}
