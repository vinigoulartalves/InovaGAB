package com.fiap.inovagab.data.repository

import com.fiap.inovagab.core.network.ApiCallRunner
import com.fiap.inovagab.data.model.Ideia
import com.fiap.inovagab.data.model.PrioridadeIdeia
import com.fiap.inovagab.data.model.StatusIdeia
import com.fiap.inovagab.data.remote.api.EstrategiasApi
import com.fiap.inovagab.data.remote.api.IdeiasApi
import com.fiap.inovagab.data.remote.dto.IdeiaAvaliacaoRequestDto
import com.fiap.inovagab.data.remote.dto.IdeiaCreateRequestDto
import com.fiap.inovagab.data.remote.fetchAllPages
import com.fiap.inovagab.data.remote.toIdeia

class IdeiaRepository(
    private val ideiasApi: IdeiasApi,
    private val estrategiasApi: EstrategiasApi
) {

    suspend fun listarTodas(): Result<List<Ideia>> = ApiCallRunner.run {
        fetchAllPages { page, size ->
            ideiasApi.list(page, size)
        }.map { it.toIdeia() }
    }

    suspend fun listarPorAutor(autorId: String): Result<List<Ideia>> = ApiCallRunner.run {
        fetchAllPages { page, size -> ideiasApi.list(page, size) }
            .map { it.toIdeia().copy(autorId = autorId) }
    }

    suspend fun criar(ideia: Ideia): Result<String> = ApiCallRunner.run {
        val estrategiaId = ideia.estrategiaId.ifBlank { resolverEstrategiaVigente() }
        val detalhe = ideiasApi.create(
            IdeiaCreateRequestDto(
                titulo = ideia.titulo,
                descricao = ideia.descricao,
                area = ideia.area,
                estrategiaId = estrategiaId
            )
        )
        detalhe.id
    }

    suspend fun atualizarPrioridade(ideia: Ideia, prioridade: PrioridadeIdeia): Result<Unit> =
        avaliar(ideia, ideia.status, prioridade)

    suspend fun atualizarStatus(ideia: Ideia, status: StatusIdeia): Result<Unit> =
        avaliar(ideia, status, ideia.prioridade)

    suspend fun atualizarStatusComPontuacao(ideia: Ideia, novoStatus: StatusIdeia): Result<Unit> =
        avaliar(ideia, novoStatus, ideia.prioridade)

    private suspend fun avaliar(
        ideia: Ideia,
        status: StatusIdeia,
        prioridade: PrioridadeIdeia
    ): Result<Unit> = ApiCallRunner.run {
        ideiasApi.avaliar(
            ideia.id,
            IdeiaAvaliacaoRequestDto(
                versao = ideia.versao,
                status = status.name,
                prioridade = prioridade.name
            )
        )
        Unit
    }

    suspend fun resolverEstrategiaVigente(): String {
        val page = estrategiasApi.list(page = 1, pageSize = 1, vigente = true)
        return page.items.firstOrNull()?.id
            ?: error("Nenhuma estratégia vigente disponível.")
    }
}
