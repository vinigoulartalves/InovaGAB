package com.fiap.inovagab.data.repository

import com.fiap.inovagab.core.network.ApiCallRunner
import com.fiap.inovagab.core.session.SessionManager
import com.fiap.inovagab.data.model.Projeto
import com.fiap.inovagab.data.remote.api.EstrategiasApi
import com.fiap.inovagab.data.remote.api.ProjetosApi
import com.fiap.inovagab.data.remote.api.UsuariosApi
import com.fiap.inovagab.data.remote.dto.ProjetoCreateRequestDto
import com.fiap.inovagab.data.remote.dto.ProjetoUpdateRequestDto
import com.fiap.inovagab.data.remote.fetchAllPages
import com.fiap.inovagab.data.remote.toMoneyBigDecimal
import com.fiap.inovagab.data.remote.toProjeto

class ProjetoRepository(
    private val projetosApi: ProjetosApi,
    private val usuariosApi: UsuariosApi,
    private val estrategiasApi: EstrategiasApi,
    private val sessionManager: SessionManager
) {

    suspend fun listar(): Result<List<Projeto>> = ApiCallRunner.run {
        fetchAllPages { page, size -> projetosApi.list(page, size) }
            .map { it.toProjeto() }
    }

    suspend fun buscarPorId(id: String): Result<Projeto?> = ApiCallRunner.run {
        projetosApi.get(id).toProjeto()
    }

    suspend fun criar(projeto: Projeto): Result<String> = ApiCallRunner.run {
        val estrategiaId = projeto.estrategiaId.ifBlank { primeiraEstrategiaVigente() }
        val responsavelId = projeto.responsavelId.ifBlank { responsavelPadrao() }
        val detalhe = projetosApi.create(
            ProjetoCreateRequestDto(
                nome = projeto.nome,
                descricao = projeto.descricao,
                estrategiaId = estrategiaId,
                responsavelId = responsavelId,
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

    suspend fun atualizar(projeto: Projeto): Result<Unit> = ApiCallRunner.run {
        val responsavelId = projeto.responsavelId.ifBlank { responsavelPadrao() }
        projetosApi.update(
            projeto.id,
            ProjetoUpdateRequestDto(
                versao = projeto.versao,
                nome = projeto.nome,
                descricao = projeto.descricao,
                responsavelId = responsavelId,
                etapa = projeto.etapa,
                status = projeto.status.name,
                investimento = projeto.investimento.toMoneyBigDecimal(),
                retornoFinanceiro = projeto.retornoFinanceiro.toMoneyBigDecimal(),
                reducaoCustos = projeto.reducaoCustos.toMoneyBigDecimal(),
                ganhoProdutividade = projeto.ganhoProdutividade.toMoneyBigDecimal(),
                prazo = projeto.prazo
            )
        )
        Unit
    }

    private suspend fun primeiraEstrategiaVigente(): String {
        val page = estrategiasApi.list(page = 1, pageSize = 1, vigente = true)
        return page.items.firstOrNull()?.id ?: error("Estratégia vigente não encontrada.")
    }

    private suspend fun responsavelPadrao(): String {
        val gestorId = sessionManager.currentUser.value?.uid
        if (!gestorId.isNullOrBlank()) {
            val lista = usuariosApi.responsaveis()
            if (lista.any { it.id == gestorId }) return gestorId
            return lista.firstOrNull()?.id ?: gestorId
        }
        return usuariosApi.responsaveis().firstOrNull()?.id
            ?: error("Nenhum gestor responsável disponível.")
    }
}
