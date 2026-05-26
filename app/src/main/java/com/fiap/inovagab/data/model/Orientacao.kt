package com.fiap.inovagab.data.model

data class Orientacao(
    val id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val criadoEm: Long = System.currentTimeMillis()
)
