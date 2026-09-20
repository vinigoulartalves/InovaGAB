using InovaGAB.Api.Auth;
using InovaGAB.Api.Http;
using InovaGAB.Application.Common;
using InovaGAB.Application.Exceptions;
using InovaGAB.Application.Projetos;
using InovaGAB.Application.Projetos.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InovaGAB.Api.Controllers.V1;

[ApiController]
[Route("api/v1/projetos")]
[Authorize]
public sealed class ProjetosController : ControllerBase
{
    private readonly IProjetoService _service;

    public ProjetosController(IProjetoService service) => _service = service;

    [HttpGet]
    public Task<PagedResultDto<ProjetoResumoDto>> List(
        [FromQuery] ProjetoListQuery query,
        CancellationToken cancellationToken)
    {
        var perfil = CurrentUserAccessor.GetPerfil(User);
        return _service.ListAsync(query, perfil, cancellationToken);
    }

    [HttpGet("{id}")]
    public Task<ProjetoDetalheDto> Get(string id, CancellationToken cancellationToken)
    {
        var perfil = CurrentUserAccessor.GetPerfil(User);
        return _service.GetAsync(id, perfil, cancellationToken);
    }

    [HttpPost]
    [Authorize(Policy = AuthPolicies.Gestor)]
    public Task<ProjetoDetalheDto> Create(
        [FromBody] ProjetoCreateRequestDto request,
        CancellationToken cancellationToken)
    {
        if (!string.IsNullOrWhiteSpace(request.IdeiaId))
        {
            throw new BusinessException(
                400,
                "VALIDACAO",
                "Criação direta de projeto não aceita ideiaId; use a conversão.");
        }

        return _service.CreateAsync(request, cancellationToken);
    }

    [HttpPut("{id}")]
    [Authorize(Policy = AuthPolicies.Gestor)]
    public Task<ProjetoDetalheDto> Update(
        string id,
        [FromBody] ProjetoUpdateRequestDto request,
        CancellationToken cancellationToken) =>
        _service.UpdateAsync(id, request, cancellationToken);

    [HttpDelete("{id}")]
    [Authorize(Policy = AuthPolicies.Gestor)]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    public async Task<IActionResult> Delete(string id, CancellationToken cancellationToken)
    {
        if (!ConcurrencyHeaderParser.TryParseIfMatch(Request.Headers.IfMatch, out var versao))
        {
            return BadRequest(new { code = "VALIDACAO", detail = "If-Match com versão é obrigatório." });
        }

        await _service.DeleteLogicalAsync(id, versao, cancellationToken);
        return NoContent();
    }
}
