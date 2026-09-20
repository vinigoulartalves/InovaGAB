package com.fiap.inovagab.data.repository

import com.fiap.inovagab.core.network.ApiCallRunner
import com.fiap.inovagab.data.model.Ideia
import com.fiap.inovagab.data.model.Orientacao
import com.fiap.inovagab.data.model.PrioridadeIdeia
import com.fiap.inovagab.data.model.Projeto
import com.fiap.inovagab.data.model.StatusIdeia
import com.fiap.inovagab.data.remote.api.EstrategiasApi
import com.fiap.inovagab.data.remote.api.IdeiasApi
import com.fiap.inovagab.data.remote.dto.ConversaoIdeiaProjetoRequestDto
import com.fiap.inovagab.data.remote.dto.IdeiaAvaliacaoRequestDto
import com.fiap.inovagab.data.remote.dto.IdeiaCreateRequestDto
import com.fiap.inovagab.data.remote.dto.IdeiaUpdateRequestDto
import com.fiap.inovagab.data.remote.fetchAllPages
import com.fiap.inovagab.data.remote.toIdeia
import com.fiap.inovagab.data.remote.toMoneyBigDecimal
import com.fiap.inovagab.data.remote.toOrientacao
import com.fiap.inovagab.data.remote.toProjeto

class IdeiaRepository(
    private val ideiasApi: IdeiasApi,
    private val estrategiasApi: EstrategiasApi
) {

    suspend fun listarTodas(): Result<List<Ideia>> = ApiCallRunner.run {
        fetchAllPages { page, size -> ideiasApi.list(page, size) }
            .map { it.toIdeia() }
    }

    suspend fun listarPorAutor(autorId: String): Result<List<Ideia>> = ApiCallRunner.run {
        fetchAllPages { page, size -> ideiasApi.list(page, size, autorId = autorId) }
            .map { it.toIdeia() }
    }

    suspend fun buscarPorId(id: String): Result<Ideia?> = ApiCallRunner.run {
        ideiasApi.get(id).toIdeia()
    }

    suspend fun listarEstrategiasVigentes(): Result<List<Orientacao>> = ApiCallRunner.run {
        fetchAllPages { page, size -> estrategiasApi.list(page, size, vigente = true) }
            .map { it.toOrientacao() }
            .filter { it.selecionavelParaNovoVinculo }
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

    suspend fun atualizar(ideia: Ideia): Result<Unit> = ApiCallRunner.run {
        ideiasApi.update(
            ideia.id,
            IdeiaUpdateRequestDto(
                versao = ideia.versao,
                titulo = ideia.titulo,
                descricao = ideia.descricao,
                area = ideia.area
            )
        )
        Unit
    }

    suspend fun excluir(ideia: Ideia): Result<Unit> = ApiCallRunner.run {
        ideiasApi.delete(ideia.id, "W/\"${ideia.versao}\"")
        Unit
    }

    suspend fun atualizarPrioridade(
        ideia: Ideia,
        prioridade: PrioridadeIdeia,
        justificativa: String? = null
    ): Result<Unit> = avaliar(ideia, ideia.status, prioridade, justificativa)

    suspend fun atualizarStatus(ideia: Ideia, status: StatusIdeia): Result<Unit> =
        avaliar(ideia, status, ideia.prioridade)

    suspend fun atualizarStatusComPontuacao(ideia: Ideia, novoStatus: StatusIdeia): Result<Unit> =
        avaliar(ideia, novoStatus, ideia.prioridade)

    suspend fun aplicarPrioridadeSugerida(
        ideia: Ideia,
        prioridade: PrioridadeIdeia,
        justificativa: String
    ): Result<Unit> = avaliar(ideia, ideia.status, prioridade, justificativa)

    suspend fun converterEmProjeto(ideia: Ideia, projeto: Projeto): Result<String> = ApiCallRunner.run {
        val detalhe = ideiasApi.converterEmProjeto(
            ideia.id,
            ConversaoIdeiaProjetoRequestDto(
                versao = ideia.versao,
                nome = projeto.nome,
                descricao = projeto.descricao,
                responsavelId = projeto.responsavelId,
                etapa = projeto.etapa,
                status = projeto.status.name,
                investimento = projeto.investimento.toMoneyBigDecimal(),
                retornoFinanceiro = projeto.retornoFinanceiro.toMoneyBigDecimal(),
                reducaoCustos = projeto.reducaoCustos.toMoneyBigDecimal(),
                ganhoProdutividade = projeto.ganhoProdutividade.toMoneyBigDecimal(),
                prazo = projeto.prazo
            )
        )
        detalhe.id
    }

    private suspend fun avaliar(
        ideia: Ideia,
        status: StatusIdeia,
        prioridade: PrioridadeIdeia,
        justificativa: String? = null
    ): Result<Unit> = ApiCallRunner.run {
        ideiasApi.avaliar(
            ideia.id,
            IdeiaAvaliacaoRequestDto(
                versao = ideia.versao,
                status = status.name,
                prioridade = prioridade.name,
                justificativa = justificativa
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
