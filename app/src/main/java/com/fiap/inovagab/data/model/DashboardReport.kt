package com.fiap.inovagab.data.model

data class SerieInvestimentoRetorno(
    val estrategiaId: String,
    val estrategiaTitulo: String,
    val investimento: Double,
    val retorno: Double
)

data class DistribuicaoPorStatus(
    val status: StatusProjeto,
    val quantidade: Int
)

data class DashboardReport(
    val totalProjetos: Int,
    val investimentoTotal: Double,
    val retornoTotal: Double,
    val lucroObtido: Double,
    val roiGeral: Double?,
    val reducaoCustosTotal: Double,
    val ganhoProdutividadeMedio: Double?,
    val projetosAtrasados: Int,
    val investimentoRetornoPorEstrategia: List<SerieInvestimentoRetorno>,
    val distribuicaoPorStatus: List<DistribuicaoPorStatus>
)

data class DashboardFiltros(
    val estrategiaId: String? = null,
    val projetoId: String? = null,
    val inicio: String? = null,
    val fim: String? = null
)
