package com.fiap.inovagab.data.model

enum class StatusProjeto {
    PLANEJADO,
    EM_ANDAMENTO,
    CONCLUIDO,
    CANCELADO
}

data class Projeto(
    val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val ideiaId: String = "",
    val responsavel: String = "",
    val etapa: String = "",
    val status: StatusProjeto = StatusProjeto.PLANEJADO,
    val investimento: Double = 0.0,
    val retornoFinanceiro: Double = 0.0,
    val reducaoCustos: Double = 0.0,
    val ganhoProdutividade: Double = 0.0,
    val prazo: String = "",
    val criadoEm: Long = System.currentTimeMillis()
)
