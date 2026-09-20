using InovaGAB.Application.Projetos.Dtos;
using InovaGAB.Domain.Projetos;

namespace InovaGAB.Infrastructure.Projetos;

internal static class ProjetoMapper
{
    public static ProjetoResumoDto ToResumo(Projeto entity) => new()
    {
        Id = entity.Id,
        Nome = entity.Nome,
        Status = entity.Status,
        EstrategiaId = entity.EstrategiaId,
        ResponsavelId = entity.ResponsavelId,
        Investimento = entity.Investimento,
        RetornoFinanceiro = entity.RetornoFinanceiro,
        CriadoEm = entity.CriadoEmUtc
    };

    public static ProjetoDetalheDto ToDetalhe(Projeto entity, string responsavelNome)
    {
        var resumo = ToResumo(entity);
        return new ProjetoDetalheDto
        {
            Id = resumo.Id,
            Nome = resumo.Nome,
            Status = resumo.Status,
            EstrategiaId = resumo.EstrategiaId,
            ResponsavelId = resumo.ResponsavelId,
            Investimento = resumo.Investimento,
            RetornoFinanceiro = resumo.RetornoFinanceiro,
            CriadoEm = resumo.CriadoEm,
            Descricao = entity.Descricao,
            IdeiaId = entity.IdeiaId,
            EstrategiaVersao = entity.EstrategiaVersao,
            ResponsavelNome = responsavelNome,
            Etapa = entity.Etapa,
            ReducaoCustos = entity.ReducaoCustos,
            GanhoProdutividade = entity.GanhoProdutividade,
            Prazo = entity.Prazo,
            Versao = entity.Versao,
            ExcluidaEm = entity.ExcluidaEmUtc,
            AtualizadoEm = entity.AtualizadoEmUtc
        };
    }
}
