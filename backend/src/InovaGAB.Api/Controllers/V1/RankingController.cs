using InovaGAB.Application.Ranking;
using InovaGAB.Application.Ranking.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace InovaGAB.Api.Controllers.V1;

[ApiController]
[Route("api/v1/ranking")]
[Authorize]
public sealed class RankingController : ControllerBase
{
    private readonly IRankingService _service;

    public RankingController(IRankingService service) => _service = service;

    [HttpGet]
    public Task<RankingResponseDto> Get(CancellationToken cancellationToken) =>
        _service.GetRankingAsync(cancellationToken);
}
