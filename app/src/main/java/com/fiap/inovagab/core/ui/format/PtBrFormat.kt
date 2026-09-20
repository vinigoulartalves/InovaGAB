package com.fiap.inovagab.core.ui.format

import java.text.NumberFormat
import java.util.Locale

private val ptBr = Locale("pt", "BR")

fun formatarMoedaPtBr(valor: Double): String =
    NumberFormat.getCurrencyInstance(ptBr).format(valor)

fun formatarPercentualPtBr(valor: Double): String {
    val formato = NumberFormat.getNumberInstance(ptBr)
    formato.maximumFractionDigits = 2
    formato.minimumFractionDigits = 0
    return "${formato.format(valor)}%"
}

fun formatarRoiPtBr(roi: Double?): String =
    if (roi == null) "Não aplicável" else formatarPercentualPtBr(roi)

fun formatarPercentualOpcionalPtBr(valor: Double?): String =
    if (valor == null) "Não aplicável" else formatarPercentualPtBr(valor)
