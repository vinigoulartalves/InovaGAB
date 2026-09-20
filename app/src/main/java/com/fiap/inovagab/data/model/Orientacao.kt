package com.fiap.inovagab.data.model

data class Orientacao(
    val id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val categoria: String = "",
    val campanha: String = "",
    val inicioVigencia: String = "",
    val fimVigencia: String? = null,
    val ativa: Boolean = true,
    val vigente: Boolean = false,
    val versao: Int = 1,
    val arquivada: Boolean = false,
    val criadoEm: Long = System.currentTimeMillis()
) {
    val selecionavelParaNovoVinculo: Boolean get() = vigente && !arquivada
}
