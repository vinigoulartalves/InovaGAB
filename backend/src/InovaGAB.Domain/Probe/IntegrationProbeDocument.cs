namespace InovaGAB.Domain.Probe;

/// <summary>
/// Documento técnico usado apenas para validar EF Core + MongoDB na fundação.
/// Não exposto como módulo de negócio.
/// </summary>
public sealed class IntegrationProbeDocument
{
    public string Id { get; set; } = string.Empty;

    public string Name { get; set; } = string.Empty;

    public decimal Amount { get; set; }

    public ProbeDocumentStatus Status { get; set; }

    public DateOnly BusinessDate { get; set; }

    public DateTime CreatedAtUtc { get; set; }

    public int Versao { get; set; }
}
