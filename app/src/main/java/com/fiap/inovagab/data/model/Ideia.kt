package com.fiap.inovagab.data.model

enum class StatusIdeia {
    NOVA,
    EM_ANALISE,
    APROVADA,
    REJEITADA,
    VIROU_PROJETO
}

data class Ideia(
    val id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val area: String = "",
    val autorUid: String = "",
    val autorNome: String = "",
    val orientacaoId: String = "",
    val status: StatusIdeia = StatusIdeia.NOVA,
    val criadoEm: Long = System.currentTimeMillis()
)
