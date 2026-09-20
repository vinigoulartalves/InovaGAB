package com.fiap.inovagab.data.model

data class AnaliseIaResumo(
    val id: String,
    val pontuacaoTotal: Int,
    val prioridadeSugerida: PrioridadeIdeia,
    val desatualizada: Boolean,
    val criadoEm: Long
)
