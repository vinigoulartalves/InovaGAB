package com.fiap.inovagab.ui.gestor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fiap.inovagab.core.di.inovaViewModel
import com.fiap.inovagab.core.testing.TestTags
import com.fiap.inovagab.core.ui.components.AppButton
import com.fiap.inovagab.core.ui.components.AppCard
import com.fiap.inovagab.data.model.AnaliseIa
import com.fiap.inovagab.core.ui.effects.OnResumeEffect
import com.fiap.inovagab.data.model.Ideia
import com.fiap.inovagab.data.model.PrioridadeIdeia
import com.fiap.inovagab.data.model.StatusIdeia

private val StatusEditaveis = listOf(
    StatusIdeia.EM_ANALISE,
    StatusIdeia.APROVADA,
    StatusIdeia.REJEITADA
)

private val PrioridadesEditaveis = listOf(
    PrioridadeIdeia.BAIXA,
    PrioridadeIdeia.MEDIA,
    PrioridadeIdeia.ALTA
)

@Composable
fun GestaoIdeiasScreen(
    onBack: () -> Unit = {},
    onConverterProjeto: (String) -> Unit = {},
    viewModel: GestorViewModel = inovaViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OnResumeEffect {
        viewModel.carregarIdeias()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Gestão de Ideias",
            color = Color(0xFF002B5C),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Avalie as ideias enviadas, defina prioridades e aprove para somar pontos ao autor.",
            color = Color(0xFF4A5A6E),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            state.carregando && state.ideias.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF002B5C))
                }
            }

            state.erro != null && state.ideias.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.erro ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.carregarIdeias() }) {
                        Text(text = "Tentar novamente")
                    }
                }
            }

            state.ideias.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma ideia cadastrada ainda.",
                        color = Color(0xFF4A5A6E),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            else -> {
                if (state.erro != null) {
                    Text(
                        text = state.erro ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (state.mensagem != null) {
                    Text(
                        text = state.mensagem ?: "",
                        color = Color(0xFF1B7F3B),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag(TestTags.GESTAO_IDEIAS_LISTA)
                ) {
                    items(state.ideias, key = { it.id }) { ideia ->
                        val iaPainel = state.iaPorIdeia[ideia.id] ?: IaPainelUiState()
                        IdeiaGestaoCard(
                            ideia = ideia,
                            iaPainel = iaPainel,
                            onAlterarPrioridade = { nova ->
                                viewModel.alterarPrioridade(ideia, nova)
                            },
                            onAlterarStatus = { novo ->
                                viewModel.alterarStatus(ideia, novo)
                            },
                            onAnalisarIa = { viewModel.analisarComIa(ideia) },
                            onAplicarPrioridadeIa = { analise ->
                                viewModel.aplicarPrioridadeSugerida(ideia, analise)
                            },
                            onConverterProjeto = {
                                if (ideia.status == StatusIdeia.APROVADA) {
                                    onConverterProjeto(ideia.id)
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = "Voltar")
        }
    }
}

@Composable
private fun IdeiaGestaoCard(
    ideia: Ideia,
    iaPainel: IaPainelUiState,
    onAlterarPrioridade: (PrioridadeIdeia) -> Unit,
    onAlterarStatus: (StatusIdeia) -> Unit,
    onAnalisarIa: () -> Unit,
    onAplicarPrioridadeIa: (AnaliseIa) -> Unit,
    onConverterProjeto: () -> Unit
) {
    AppCard {
        Text(
            text = ideia.titulo,
            color = Color(0xFF002B5C),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (ideia.descricao.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = ideia.descricao,
                color = Color(0xFF2C3E50),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (ideia.area.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Área: ${ideia.area}",
                color = Color(0xFF4A5A6E),
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (ideia.autorNome.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Autor: ${ideia.autorNome}",
                color = Color(0xFF4A5A6E),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Etiqueta(
                texto = "Status: ${formatarStatus(ideia.status)}",
                cor = corDoStatus(ideia.status)
            )
            Etiqueta(
                texto = "Prioridade: ${formatarPrioridade(ideia.prioridade)}",
                cor = corDaPrioridade(ideia.prioridade)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrioridadeSelector(
                atual = ideia.prioridade,
                onSelecionar = onAlterarPrioridade,
                modifier = Modifier.weight(1f)
            )
            StatusSelector(
                atual = ideia.status,
                onSelecionar = onAlterarStatus,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AppButton(
            text = if (iaPainel.carregando) "Analisando com IA…" else "Analisar com IA",
            onClick = onAnalisarIa,
            loading = iaPainel.carregando,
            enabled = !iaPainel.carregando,
            modifier = Modifier.testTag(TestTags.GESTAO_ANALISAR_IA)
        )

        if (iaPainel.erro != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = iaPainel.erro ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        iaPainel.analise?.let { analise ->
            Spacer(modifier = Modifier.height(8.dp))
            PainelAnaliseIa(
                analise = analise,
                onAplicarPrioridade = { onAplicarPrioridadeIa(analise) }
            )
        }

        if (ideia.status == StatusIdeia.APROVADA) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onConverterProjeto,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.GESTAO_CONVERTER_PROJETO)
            ) {
                Text(text = "Converter em projeto")
            }
        }
    }
}

@Composable
private fun PainelAnaliseIa(
    analise: AnaliseIa,
    onAplicarPrioridade: () -> Unit
) {
    Surface(
        color = Color(0xFFE8EEF5),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Análise IA — nota ${analise.pontuacaoTotal}/100",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF002B5C)
            )
            if (analise.desatualizada) {
                Text(
                    text = "Indicador: análise desatualizada (ideia alterada após a análise).",
                    color = Color(0xFFB26A00),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Prioridade sugerida: ${formatarPrioridade(analise.prioridadeSugerida)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = analise.justificativa,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2C3E50)
            )
            if (analise.riscos.isNotEmpty()) {
                Text(
                    text = "Riscos: ${analise.riscos.joinToString("; ")}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (analise.melhorias.isNotEmpty()) {
                Text(
                    text = "Melhorias: ${analise.melhorias.joinToString("; ")}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            val meta = listOfNotNull(analise.modelo, analise.provedor)
                .joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(
                    text = "Modelo: $meta",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4A5A6E)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onAplicarPrioridade,
                modifier = Modifier.testTag(TestTags.GESTAO_APLICAR_PRIORIDADE_IA)
            ) {
                Text(text = "Aplicar prioridade sugerida (avaliação)")
            }
        }
    }
}

@Composable
private fun PrioridadeSelector(
    atual: PrioridadeIdeia,
    onSelecionar: (PrioridadeIdeia) -> Unit,
    modifier: Modifier = Modifier
) {
    var aberto by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { aberto = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Prioridade: ${formatarPrioridade(atual)}")
        }
        DropdownMenu(
            expanded = aberto,
            onDismissRequest = { aberto = false }
        ) {
            PrioridadesEditaveis.forEach { prioridade ->
                DropdownMenuItem(
                    text = { Text(text = formatarPrioridade(prioridade)) },
                    onClick = {
                        aberto = false
                        onSelecionar(prioridade)
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusSelector(
    atual: StatusIdeia,
    onSelecionar: (StatusIdeia) -> Unit,
    modifier: Modifier = Modifier
) {
    var aberto by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { aberto = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Status: ${formatarStatus(atual)}")
        }
        DropdownMenu(
            expanded = aberto,
            onDismissRequest = { aberto = false }
        ) {
            StatusEditaveis.forEach { status ->
                DropdownMenuItem(
                    text = { Text(text = formatarStatus(status)) },
                    onClick = {
                        aberto = false
                        onSelecionar(status)
                    }
                )
            }
        }
    }
}

@Composable
private fun Etiqueta(texto: String, cor: Color) {
    Surface(
        color = cor.copy(alpha = 0.15f),
        contentColor = cor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private fun formatarStatus(status: StatusIdeia): String = when (status) {
    StatusIdeia.ENVIADA -> "Enviada"
    StatusIdeia.EM_ANALISE -> "Em análise"
    StatusIdeia.APROVADA -> "Aprovada"
    StatusIdeia.REJEITADA -> "Rejeitada"
    StatusIdeia.VIROU_PROJETO -> "Virou projeto"
}

private fun formatarPrioridade(prioridade: PrioridadeIdeia): String = when (prioridade) {
    PrioridadeIdeia.BAIXA -> "Baixa"
    PrioridadeIdeia.MEDIA -> "Média"
    PrioridadeIdeia.ALTA -> "Alta"
}

private fun corDoStatus(status: StatusIdeia): Color = when (status) {
    StatusIdeia.ENVIADA -> Color(0xFF1565C0)
    StatusIdeia.EM_ANALISE -> Color(0xFFB26A00)
    StatusIdeia.APROVADA -> Color(0xFF1B7F3B)
    StatusIdeia.REJEITADA -> Color(0xFFB00020)
    StatusIdeia.VIROU_PROJETO -> Color(0xFF6A1B9A)
}

private fun corDaPrioridade(prioridade: PrioridadeIdeia): Color = when (prioridade) {
    PrioridadeIdeia.BAIXA -> Color(0xFF1B7F3B)
    PrioridadeIdeia.MEDIA -> Color(0xFFB26A00)
    PrioridadeIdeia.ALTA -> Color(0xFFB00020)
}
