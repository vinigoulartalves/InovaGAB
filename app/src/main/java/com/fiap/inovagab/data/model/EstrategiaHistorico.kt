package com.fiap.inovagab.data.model

data class EstrategiaHistorico(
    val id: String,
    val versao: Int,
    val acao: String,
    val atorId: String,
    val ocorridoEm: Long
)
