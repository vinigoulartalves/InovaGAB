using InovaGAB.Api.Auth;
using InovaGAB.Application.Auth;
using InovaGAB.Application.Auth.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InovaGAB.Api.Controllers.V1;

[ApiController]
[Route("api/v1/usuarios")]
[Authorize]
public sealed class UsuariosController : ControllerBase
{
    private readonly IAuthService _authService;

    public UsuariosController(IAuthService authService)
    {
        _authService = authService;
    }

    [HttpGet("responsaveis")]
    [Authorize(Policy = AuthPolicies.Gestor)]
    [ProducesResponseType(typeof(IReadOnlyList<ResponsavelResumoDto>), StatusCodes.Status200OK)]
    public async Task<ActionResult<IReadOnlyList<ResponsavelResumoDto>>> ListResponsaveis(
        CancellationToken cancellationToken)
    {
        var items = await _authService.ListResponsaveisAsync(cancellationToken);
        return Ok(items);
    }
}
