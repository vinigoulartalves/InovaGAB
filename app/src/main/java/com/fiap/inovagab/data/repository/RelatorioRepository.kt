package com.fiap.inovagab.data.repository

import com.fiap.inovagab.core.network.ApiCallRunner
import com.fiap.inovagab.data.model.DashboardFiltros
import com.fiap.inovagab.data.model.DashboardReport
import com.fiap.inovagab.data.remote.api.RelatoriosApi
import com.fiap.inovagab.data.remote.toDashboardReport

class RelatorioRepository(
    private val relatoriosApi: RelatoriosApi
) {

    suspend fun carregarDashboard(filtros: DashboardFiltros = DashboardFiltros()): Result<DashboardReport> =
        ApiCallRunner.run {
            val dash = relatoriosApi.dashboard(
                estrategiaId = filtros.estrategiaId,
                projetoId = filtros.projetoId,
                inicio = filtros.inicio,
                fim = filtros.fim
            )
            val totalProjetos = dash.distribuicaoPorStatus.sumOf { it.quantidade }
            dash.toDashboardReport(totalProjetos)
        }
}
