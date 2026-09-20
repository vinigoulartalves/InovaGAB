using InovaGAB.Api.Auth;
using InovaGAB.Api.Http;
using InovaGAB.Application.Common;
using InovaGAB.Application.Estrategias;
using InovaGAB.Application.Estrategias.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InovaGAB.Api.Controllers.V1;

[ApiController]
[Route("api/v1/estrategias")]
[Authorize]
public sealed class EstrategiasController : ControllerBase
{
    private readonly IEstrategiaService _service;

    public EstrategiasController(IEstrategiaService service) => _service = service;

    [HttpGet]
    public Task<PagedResultDto<EstrategiaResumoDto>> List(
        [FromQuery] EstrategiaListQuery query,
        CancellationToken cancellationToken) =>
        _service.ListAsync(query, cancellationToken);

    [HttpGet("{id}")]
    public Task<EstrategiaDetalheDto> Get(string id, CancellationToken cancellationToken) =>
        _service.GetAsync(id, cancellationToken);

    [HttpGet("{id}/historico")]
    public Task<PagedResultDto<EstrategiaHistoricoItemDto>> Historico(
        string id,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20,
        CancellationToken cancellationToken = default) =>
        _service.GetHistoricoAsync(id, page, pageSize, cancellationToken);

    [HttpPost]
    [Authorize(Policy = AuthPolicies.Lider)]
    public Task<EstrategiaDetalheDto> Create(
        [FromBody] EstrategiaCreateRequestDto request,
        CancellationToken cancellationToken)
    {
        var atorId = CurrentUserAccessor.GetUserId(User);
        return _service.CreateAsync(request, atorId, cancellationToken);
    }

    [HttpPut("{id}")]
    [Authorize(Policy = AuthPolicies.Lider)]
    public Task<EstrategiaDetalheDto> Update(
        string id,
        [FromBody] EstrategiaUpdateRequestDto request,
        CancellationToken cancellationToken)
    {
        var atorId = CurrentUserAccessor.GetUserId(User);
        return _service.UpdateAsync(id, request, atorId, cancellationToken);
    }

    [HttpDelete("{id}")]
    [Authorize(Policy = AuthPolicies.Lider)]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    public async Task<IActionResult> Delete(string id, CancellationToken cancellationToken)
    {
        if (!ConcurrencyHeaderParser.TryParseIfMatch(Request.Headers.IfMatch, out var versao))
        {
            return BadRequest(new { code = "VALIDACAO", detail = "If-Match com versão é obrigatório." });
        }

        var atorId = CurrentUserAccessor.GetUserId(User);
        await _service.DeleteLogicalAsync(id, versao, atorId, cancellationToken);
        return NoContent();
    }
}
