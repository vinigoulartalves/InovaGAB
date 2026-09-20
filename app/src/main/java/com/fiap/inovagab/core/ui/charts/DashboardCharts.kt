package com.fiap.inovagab.core.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.fiap.inovagab.core.ui.format.formatarMoedaPtBr
import com.fiap.inovagab.data.model.DistribuicaoPorStatus
import com.fiap.inovagab.data.model.SerieInvestimentoRetorno
import com.fiap.inovagab.data.model.StatusProjeto

private val corInvestimento = Color(0xFF1565C0)
private val corRetorno = Color(0xFF1B7F3B)

@Composable
fun GraficoInvestimentoRetorno(
    series: List<SerieInvestimentoRetorno>,
    modifier: Modifier = Modifier
) {
    if (series.isEmpty()) {
        Text(
            text = "Sem dados de investimento e retorno para os filtros selecionados.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4A5A6E),
            modifier = modifier.padding(vertical = 8.dp)
        )
        return
    }

    val descricaoTexto = series.joinToString("; ") { item ->
        "${item.estrategiaTitulo}: investimento ${formatarMoedaPtBr(item.investimento)}, " +
            "retorno ${formatarMoedaPtBr(item.retorno)}"
    }

    Column(modifier = modifier.semantics { contentDescription = descricaoTexto }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendaItem(cor = corInvestimento, texto = "Investimento")
            LegendaItem(cor = corRetorno, texto = "Retorno")
        }

        val maxValor = series.maxOf { maxOf(it.investimento, it.retorno) }.coerceAtLeast(1.0)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height((series.size * 56).coerceAtLeast(120).dp)
                .padding(top = 12.dp)
        ) {
            val barHeight = size.height / (series.size * 2.5f)
            val gap = barHeight * 0.35f
            var y = gap

            series.forEach { item ->
                val investW = (item.investimento / maxValor) * size.width * 0.85f
                val retW = (item.retorno / maxValor) * size.width * 0.85f
                drawRect(
                    color = corInvestimento,
                    topLeft = Offset(0f, y),
                    size = Size(investW.toFloat(), barHeight)
                )
                drawRect(
                    color = corRetorno,
                    topLeft = Offset(0f, y + barHeight + gap / 2),
                    size = Size(retW.toFloat(), barHeight)
                )
                y += barHeight * 2 + gap * 2
            }
        }

        series.forEach { item ->
            Text(
                text = item.estrategiaTitulo,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF002B5C),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun GraficoDistribuicaoStatus(
    distribuicao: List<DistribuicaoPorStatus>,
    modifier: Modifier = Modifier
) {
    if (distribuicao.isEmpty()) {
        Text(
            text = "Sem projetos para exibir distribuição por status.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4A5A6E),
            modifier = modifier.padding(vertical = 8.dp)
        )
        return
    }

    val cores = mapOf(
        StatusProjeto.PLANEJADO to Color(0xFF1565C0),
        StatusProjeto.EM_ANDAMENTO to Color(0xFFB26A00),
        StatusProjeto.CONCLUIDO to Color(0xFF1B7F3B),
        StatusProjeto.CANCELADO to Color(0xFFB00020)
    )

    val descricaoTexto = distribuicao.joinToString("; ") {
        "${formatarStatusProjeto(it.status)}: ${it.quantidade}"
    }

    val total = distribuicao.sumOf { it.quantidade }.coerceAtLeast(1)

    Column(modifier = modifier.semantics { contentDescription = descricaoTexto }) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .padding(vertical = 8.dp)
        ) {
            var x = 0f
            distribuicao.forEach { item ->
                val fraction = item.quantidade.toFloat() / total
                val w = size.width * fraction
                drawRoundRect(
                    color = cores[item.status] ?: Color.Gray,
                    topLeft = Offset(x, 0f),
                    size = Size(w, size.height),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                x += w
            }
        }

        distribuicao.forEach { item ->
            LegendaItem(
                cor = cores[item.status] ?: Color.Gray,
                texto = "${formatarStatusProjeto(item.status)}: ${item.quantidade}"
            )
        }
    }
}

@Composable
private fun LegendaItem(cor: Color, texto: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(modifier = Modifier.height(14.dp).padding(top = 2.dp)) {
            drawCircle(color = cor, radius = 6f, center = Offset(6f, 6f))
        }
        Text(text = texto, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4A5A6E))
    }
}

private fun formatarStatusProjeto(status: StatusProjeto): String = when (status) {
    StatusProjeto.PLANEJADO -> "Planejado"
    StatusProjeto.EM_ANDAMENTO -> "Em andamento"
    StatusProjeto.CONCLUIDO -> "Concluído"
    StatusProjeto.CANCELADO -> "Cancelado"
}
