using InovaGAB.Api.Auth;
using InovaGAB.Api.Http;
using InovaGAB.Application.Common;
using InovaGAB.Application.Ideias;
using InovaGAB.Application.Ideias.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InovaGAB.Api.Controllers.V1;

[ApiController]
[Route("api/v1/ideias")]
[Authorize]
public sealed class IdeiasController : ControllerBase
{
    private readonly IIdeiaService _service;

    public IdeiasController(IIdeiaService service) => _service = service;

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
}
