namespace InovaGAB.Infrastructure.Relatorios;

internal static class RelatorioCalculos
{
    public static decimal Lucro(decimal investimento, decimal retorno) => retorno - investimento;

    public static decimal? RoiPercentual(decimal investimento, decimal lucro)
    {
        if (investimento <= 0)
        {
            return null;
        }

        return RoundPercent(lucro / investimento * 100m);
    }

    public static decimal RoundMoney(decimal value) =>
        Math.Round(value, 4, MidpointRounding.AwayFromZero);

    public static decimal RoundPercent(decimal value) =>
        Math.Round(value, 4, MidpointRounding.AwayFromZero);
}
