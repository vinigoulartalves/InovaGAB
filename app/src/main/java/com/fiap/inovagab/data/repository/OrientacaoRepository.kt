package com.fiap.inovagab.data.repository

import com.fiap.inovagab.core.network.ApiCallRunner
import com.fiap.inovagab.data.model.EstrategiaHistorico
import com.fiap.inovagab.data.model.Orientacao
import com.fiap.inovagab.data.remote.api.EstrategiasApi
import com.fiap.inovagab.data.remote.dto.EstrategiaCreateRequestDto
import com.fiap.inovagab.data.remote.dto.EstrategiaUpdateRequestDto
import com.fiap.inovagab.data.remote.fetchAllPages
import com.fiap.inovagab.data.remote.toHistorico
import com.fiap.inovagab.data.remote.toOrientacao
import java.time.LocalDate

class OrientacaoRepository(
    private val estrategiasApi: EstrategiasApi
) {

    suspend fun listar(): Result<List<Orientacao>> = ApiCallRunner.run {
        fetchAllPages { page, size -> estrategiasApi.list(page, size) }
            .map { it.toOrientacao() }
    }

    suspend fun listarVigentesSelecionaveis(): Result<List<Orientacao>> = ApiCallRunner.run {
        fetchAllPages { page, size -> estrategiasApi.list(page, size, vigente = true) }
            .map { it.toOrientacao() }
            .filter { it.selecionavelParaNovoVinculo }
    }

    suspend fun buscarPorId(id: String): Result<Orientacao?> = ApiCallRunner.run {
        estrategiasApi.get(id).toOrientacao()
    }

    suspend fun historico(estrategiaId: String): Result<List<EstrategiaHistorico>> = ApiCallRunner.run {
        fetchAllPages { page, size -> estrategiasApi.historico(estrategiaId, page, size) }
            .map { it.toHistorico() }
    }

    suspend fun criar(orientacao: Orientacao): Result<String> = ApiCallRunner.run {
        val inicio = orientacao.inicioVigencia.ifBlank { LocalDate.now().toString() }
        val detalhe = estrategiasApi.create(
            EstrategiaCreateRequestDto(
                titulo = orientacao.titulo,
                descricao = orientacao.descricao,
                categoria = orientacao.categoria.ifBlank { "Geral" },
                campanha = orientacao.campanha.ifBlank { "Campanha" },
                inicioVigencia = inicio,
                fimVigencia = orientacao.fimVigencia,
                ativa = orientacao.ativa
            )
        )
        detalhe.id
    }

    suspend fun atualizar(orientacao: Orientacao): Result<Unit> = ApiCallRunner.run {
        val atual = estrategiasApi.get(orientacao.id)
        estrategiasApi.update(
            orientacao.id,
            EstrategiaUpdateRequestDto(
                versao = atual.versao,
                titulo = orientacao.titulo,
                descricao = orientacao.descricao,
                categoria = orientacao.categoria.ifBlank { atual.categoria },
                campanha = orientacao.campanha.ifBlank { atual.campanha },
                inicioVigencia = orientacao.inicioVigencia.ifBlank { atual.inicioVigencia },
                fimVigencia = orientacao.fimVigencia ?: atual.fimVigencia,
                ativa = orientacao.ativa
            )
        )
        Unit
    }

    suspend fun excluir(id: String): Result<Unit> = ApiCallRunner.run {
        val atual = estrategiasApi.get(id)
        estrategiasApi.delete(id, "W/\"${atual.versao}\"")
        Unit
    }
}
