namespace InovaGAB.Application.Ranking.Dtos;

public sealed class RankingResponseDto
{
    public IReadOnlyList<RankingItemDto> Items { get; set; } = Array.Empty<RankingItemDto>();

    public DateTime AtualizadoEm { get; set; }
}

public sealed class RankingItemDto
{
    public int Posicao { get; set; }

    public string Nome { get; set; } = string.Empty;

    public int Pontos { get; set; }
}
