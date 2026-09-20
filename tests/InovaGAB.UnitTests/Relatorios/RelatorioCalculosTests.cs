using InovaGAB.Infrastructure.Relatorios;
using Xunit;

namespace InovaGAB.UnitTests.Relatorios;

public sealed class RelatorioCalculosTests
{
    [Theory]
    [InlineData(1000, 1500, 500)]
    [InlineData(0, 0, 0)]
    public void Lucro_calcula_retorno_menos_investimento(decimal investimento, decimal retorno, decimal esperado)
    {
        Assert.Equal(esperado, RelatorioCalculos.Lucro(investimento, retorno));
    }

    [Fact]
    public void Roi_null_quando_investimento_zero()
    {
        Assert.Null(RelatorioCalculos.RoiPercentual(0, 100));
        Assert.Null(RelatorioCalculos.RoiPercentual(-1, 100));
    }

    [Fact]
    public void Roi_percentual_arredondado()
    {
        var roi = RelatorioCalculos.RoiPercentual(3000, 1100);
        Assert.NotNull(roi);
        Assert.Equal(36.6667m, roi.Value);
    }
}
