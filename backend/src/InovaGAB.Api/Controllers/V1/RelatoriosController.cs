using InovaGAB.Api.Auth;
using InovaGAB.Application.Relatorios;
using InovaGAB.Application.Relatorios.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InovaGAB.Api.Controllers.V1;

[ApiController]
[Route("api/v1/relatorios")]
[Authorize(Policy = AuthPolicies.Lider)]
public sealed class RelatoriosController : ControllerBase
{
    private readonly IRelatorioService _service;

    public RelatoriosController(IRelatorioService service) => _service = service;

    [HttpGet("dashboard")]
    public Task<DashboardRelatorioDto> Dashboard(
        [FromQuery] string? estrategiaId,
        [FromQuery] string? projetoId,
        [FromQuery] DateOnly? inicio,
        [FromQuery] DateOnly? fim,
        CancellationToken cancellationToken) =>
        _service.GetDashboardAsync(new DashboardFiltroDto
        {
            EstrategiaId = estrategiaId,
            ProjetoId = projetoId,
            Inicio = inicio,
            Fim = fim
        }, cancellationToken);

    [HttpGet("estrategias")]
    public Task<IReadOnlyList<RelatorioEstrategiaItemDto>> PorEstrategia(
        [FromQuery] string? estrategiaId,
        [FromQuery] string? projetoId,
        [FromQuery] DateOnly? inicio,
        [FromQuery] DateOnly? fim,
        CancellationToken cancellationToken) =>
        _service.GetPorEstrategiaAsync(new DashboardFiltroDto
        {
            EstrategiaId = estrategiaId,
            ProjetoId = projetoId,
            Inicio = inicio,
            Fim = fim
        }, cancellationToken);

    [HttpGet("projetos/{id}")]
    public Task<RelatorioProjetoDetalheDto> Projeto(string id, CancellationToken cancellationToken) =>
        _service.GetProjetoAsync(id, cancellationToken);
}
