package com.fiap.inovagab.data.model

enum class StatusIdeia {
    ENVIADA,
    EM_ANALISE,
    APROVADA,
    REJEITADA,
    VIROU_PROJETO
}

enum class PrioridadeIdeia {
    BAIXA,
    MEDIA,
    ALTA
}

data class Ideia(
    val id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val area: String = "",
    val autorId: String = "",
    val autorNome: String = "",
    val status: StatusIdeia = StatusIdeia.ENVIADA,
    val prioridade: PrioridadeIdeia = PrioridadeIdeia.MEDIA,
    val criadoEm: Long = System.currentTimeMillis()
)
