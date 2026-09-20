using InovaGAB.Application.Ranking.Dtos;

namespace InovaGAB.Application.Ranking;

public interface IRankingService
{
    Task<RankingResponseDto> GetRankingAsync(CancellationToken cancellationToken);
}
