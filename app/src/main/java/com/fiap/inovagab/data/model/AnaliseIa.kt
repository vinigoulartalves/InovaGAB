package com.fiap.inovagab.data.model

data class AnaliseIa(
    val id: String,
    val ideiaId: String,
    val pontuacaoTotal: Int,
    val alinhamentoEstrategico: Int,
    val impacto: Int,
    val viabilidade: Int,
    val prioridadeSugerida: PrioridadeIdeia,
    val justificativa: String,
    val riscos: List<String>,
    val melhorias: List<String>,
    val provedor: String?,
    val modelo: String?,
    val promptVersion: String?,
    val desatualizada: Boolean,
    val criadoEm: Long
)
