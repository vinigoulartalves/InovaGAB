package com.fiap.inovagab.data.repository

import com.fiap.inovagab.core.network.ApiCallRunner
import com.fiap.inovagab.data.remote.api.ProjetosApi
import com.fiap.inovagab.data.remote.api.RelatoriosApi
import com.fiap.inovagab.data.remote.fetchAllPages
import com.fiap.inovagab.data.remote.toProjeto
import com.fiap.inovagab.ui.lider.DashboardUiState

class RelatorioRepository(
    private val relatoriosApi: RelatoriosApi,
    private val projetosApi: ProjetosApi
) {

    suspend fun carregarDashboard(): Result<DashboardUiState> = ApiCallRunner.run {
        val dash = relatoriosApi.dashboard()
        val projetos = fetchAllPages { page, size -> projetosApi.list(page, size) }
            .map { it.toProjeto() }

        DashboardUiState(
            carregando = false,
            erro = null,
            totalProjetos = projetos.size,
            investimentoTotal = dash.investimentoTotal.toDouble(),
            retornoTotal = dash.retornoTotal.toDouble(),
            lucroObtido = dash.lucroTotal.toDouble(),
            roiGeral = dash.roiPercentual?.toDouble() ?: 0.0,
            reducaoCustosTotal = dash.reducaoCustosTotal.toDouble(),
            ganhoProdutividadeMedio = dash.ganhoProdutividadeMedio?.toDouble() ?: 0.0
        )
    }
}
