using InovaGAB.Api.Auth;
using InovaGAB.Api.Http;
using InovaGAB.Application.Common;
using InovaGAB.Application.Ideias;
using InovaGAB.Application.Ideias.Dtos;
using InovaGAB.Application.Projetos;
using InovaGAB.Application.Projetos.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;

namespace InovaGAB.Api.Controllers.V1;

[ApiController]
[Route("api/v1/ideias")]
[Authorize]
public sealed class IdeiasController : ControllerBase
{
    private readonly IIdeiaService _service;
    private readonly IProjetoService _projetoService;
    private readonly IIdeaAnalysisService _ideaAnalysisService;

    public IdeiasController(
        IIdeiaService service,
        IProjetoService projetoService,
        IIdeaAnalysisService ideaAnalysisService)
    {
        _service = service;
        _projetoService = projetoService;
        _ideaAnalysisService = ideaAnalysisService;
    }

    [HttpGet]
    public Task<PagedResultDto<IdeiaResumoDto>> List(
        [FromQuery] IdeiaListQuery query,
        CancellationToken cancellationToken)
    {
        var userId = CurrentUserAccessor.GetUserId(User);
        var perfil = CurrentUserAccessor.GetPerfil(User);
        return _service.ListAsync(query, userId, perfil, cancellationToken);
    }

    [HttpGet("{id}")]
    public Task<IdeiaDetalheDto> Get(string id, CancellationToken cancellationToken)
    {
        var userId = CurrentUserAccessor.GetUserId(User);
        var perfil = CurrentUserAccessor.GetPerfil(User);
        return _service.GetAsync(id, userId, perfil, cancellationToken);
    }

    [HttpPost]
    [Authorize(Policy = AuthPolicies.Operador)]
    public Task<IdeiaDetalheDto> Create(
        [FromBody] IdeiaCreateRequestDto request,
        CancellationToken cancellationToken)
    {
        var autorId = CurrentUserAccessor.GetUserId(User);
        return _service.CreateAsync(request, autorId, cancellationToken);
    }

    [HttpPut("{id}")]
    [Authorize(Policy = AuthPolicies.Operador)]
    public Task<IdeiaDetalheDto> Update(
        string id,
        [FromBody] IdeiaUpdateRequestDto request,
        CancellationToken cancellationToken)
    {
        var autorId = CurrentUserAccessor.GetUserId(User);
        return _service.UpdateAsync(id, request, autorId, cancellationToken);
    }

    [HttpDelete("{id}")]
    [Authorize(Policy = AuthPolicies.Operador)]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    public async Task<IActionResult> Delete(string id, CancellationToken cancellationToken)
    {
        if (!ConcurrencyHeaderParser.TryParseIfMatch(Request.Headers.IfMatch, out var versao))
        {
            return BadRequest(new { code = "VALIDACAO", detail = "If-Match com versão é obrigatório." });
        }

        var autorId = CurrentUserAccessor.GetUserId(User);
        await _service.DeleteLogicalAsync(id, versao, autorId, cancellationToken);
        return NoContent();
    }

    [HttpPatch("{id}/avaliacao")]
    [Authorize(Policy = AuthPolicies.Gestor)]
    public Task<IdeiaDetalheDto> Avaliar(
        string id,
        [FromBody] IdeiaAvaliacaoRequestDto request,
        CancellationToken cancellationToken)
    {
        var gestorId = CurrentUserAccessor.GetUserId(User);
        return _service.AvaliarAsync(id, request, gestorId, cancellationToken);
    }

    [HttpPost("{id}/projeto")]
    [Authorize(Policy = AuthPolicies.Gestor)]
    [ProducesResponseType(typeof(ProjetoDetalheDto), StatusCodes.Status201Created)]
    public async Task<ActionResult<ProjetoDetalheDto>> ConverterEmProjeto(
        string id,
        [FromBody] ConversaoIdeiaProjetoRequestDto request,
        CancellationToken cancellationToken)
    {
        var projeto = await _projetoService.ConverterIdeiaAsync(id, request, cancellationToken);
        return CreatedAtAction(
            nameof(ProjetosController.Get),
            "Projetos",
            new { id = projeto.Id },
            projeto);
    }

    [HttpPost("{id}/analises-ia")]
    [Authorize(Policy = AuthPolicies.Gestor)]
    [EnableRateLimiting("ia-analise")]
    [ProducesResponseType(typeof(AnaliseIaDetalheDto), StatusCodes.Status201Created)]
    [ProducesResponseType(StatusCodes.Status503ServiceUnavailable)]
    public async Task<ActionResult<AnaliseIaDetalheDto>> SolicitarAnaliseIa(
        string id,
        CancellationToken cancellationToken)
    {
        var analise = await _ideaAnalysisService.SolicitarAnaliseAsync(id, cancellationToken);
        return Created($"/api/v1/ideias/{id}/analises-ia/{analise.Id}", analise);
    }

    [HttpGet("{id}/analises-ia")]
    [Authorize(Policy = AuthPolicies.Gestor)]
    public Task<PagedResultDto<AnaliseIaResumoDto>> ObterAnaliseIaHistorico(
        string id,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20,
        CancellationToken cancellationToken = default) =>
        _ideaAnalysisService.ListarHistoricoAsync(id, page, pageSize, cancellationToken);
}
