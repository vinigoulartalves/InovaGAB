package com.fiap.inovagab.data.model

enum class StatusProjeto {
    PLANEJAMENTO,
    EM_ANDAMENTO,
    CONCLUIDO,
    CANCELADO
}

data class Projeto(
    val id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val ideiaOrigemId: String = "",
    val responsavelUid: String = "",
    val responsavelNome: String = "",
    val status: StatusProjeto = StatusProjeto.PLANEJAMENTO,
    val criadoEm: Long = System.currentTimeMillis()
)
