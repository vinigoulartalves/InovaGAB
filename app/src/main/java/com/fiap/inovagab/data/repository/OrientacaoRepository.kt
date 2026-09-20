package com.fiap.inovagab.data.repository

import com.fiap.inovagab.core.network.ApiCallRunner
import com.fiap.inovagab.data.model.Orientacao
import com.fiap.inovagab.data.remote.api.EstrategiasApi
import com.fiap.inovagab.data.remote.dto.EstrategiaCreateRequestDto
import com.fiap.inovagab.data.remote.dto.EstrategiaUpdateRequestDto
import com.fiap.inovagab.data.remote.fetchAllPages
import com.fiap.inovagab.data.remote.toOrientacao
import java.time.LocalDate

class OrientacaoRepository(
    private val estrategiasApi: EstrategiasApi
) {

    suspend fun listar(): Result<List<Orientacao>> = ApiCallRunner.run {
        fetchAllPages { page, size -> estrategiasApi.list(page, size) }
            .map { it.toOrientacao() }
    }

    suspend fun buscarPorId(id: String): Result<Orientacao?> = ApiCallRunner.run {
        estrategiasApi.get(id).toOrientacao()
    }

    suspend fun criar(orientacao: Orientacao): Result<String> = ApiCallRunner.run {
        val hoje = LocalDate.now()
        val detalhe = estrategiasApi.create(
            EstrategiaCreateRequestDto(
                titulo = orientacao.titulo,
                descricao = orientacao.descricao,
                categoria = "Orientação",
                campanha = "App",
                inicioVigencia = hoje.toString(),
                fimVigencia = hoje.plusYears(1).toString(),
                ativa = true
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
                categoria = atual.categoria,
                campanha = atual.campanha,
                inicioVigencia = atual.inicioVigencia,
                fimVigencia = atual.fimVigencia,
                ativa = atual.ativa
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
