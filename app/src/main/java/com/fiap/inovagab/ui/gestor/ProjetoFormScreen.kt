package com.fiap.inovagab.ui.gestor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fiap.inovagab.core.di.inovaViewModel
import com.fiap.inovagab.core.testing.TestTags
import com.fiap.inovagab.core.ui.components.AppButton
import com.fiap.inovagab.core.ui.components.AppTextField
import com.fiap.inovagab.data.model.StatusProjeto

@Composable
fun ProjetoFormScreen(
    projetoId: String? = null,
    ideiaConversaoId: String? = null,
    onBack: () -> Unit = {},
    onSucesso: () -> Unit = {},
    viewModel: GestorViewModel = inovaViewModel()
) {
    val state by viewModel.projetoFormState.collectAsStateWithLifecycle()
    var confirmarExclusao by remember { mutableStateOf(false) }

    LaunchedEffect(projetoId, ideiaConversaoId) {
        viewModel.iniciarFormularioProjeto(projetoId, ideiaConversaoId)
    }

    LaunchedEffect(state.concluido) {
        if (state.concluido) {
            viewModel.consumirNavegacaoProjeto()
            onSucesso()
        }
    }

    val emEdicao = state.isEdicao
    val ocupado = state.salvando || state.carregando

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = when {
                state.modoConversao -> "Converter ideia em projeto"
                emEdicao -> "Editar projeto"
                else -> "Novo projeto"
            },
            color = Color(0xFF002B5C),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Informe os dados do projeto/iniciativa.",
            color = Color(0xFF4A5A6E),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (state.carregando) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF002B5C))
            }
        } else {
            AppTextField(
                value = state.nome,
                onValueChange = viewModel::onProjetoNomeChange,
                label = "Nome",
                enabled = !ocupado
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.descricao,
                onValueChange = viewModel::onProjetoDescricaoChange,
                label = "Descrição",
                singleLine = false,
                enabled = !ocupado
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.bloquearEstrategia) {
                AppTextField(
                    value = state.estrategiaTitulo,
                    onValueChange = {},
                    label = "Estratégia de origem (bloqueada)",
                    enabled = false
                )
            } else {
                EstrategiaProjetoSelector(
                    estrategias = state.estrategias,
                    selecionadaId = state.estrategiaId,
                    onSelecionar = { id, titulo ->
                        viewModel.onProjetoEstrategiaChange(id, titulo)
                    },
                    enabled = !ocupado,
                    modifier = Modifier.testTag(TestTags.PROJETO_FORM_ESTRATEGIA)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ResponsavelProjetoSelector(
                responsaveis = state.responsaveis,
                selecionadoId = state.responsavelId,
                onSelecionar = { id, nome -> viewModel.onProjetoResponsavelIdChange(id, nome) },
                enabled = !ocupado,
                modifier = Modifier.testTag(TestTags.PROJETO_FORM_RESPONSAVEL)
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.etapa,
                onValueChange = viewModel::onProjetoEtapaChange,
                label = "Etapa (ex.: Iniciação, Execução, Encerramento)",
                enabled = !ocupado
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatusProjetoSelector(
                atual = state.status,
                onSelecionar = viewModel::onProjetoStatusChange,
                enabled = !ocupado
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.investimento,
                onValueChange = viewModel::onProjetoInvestimentoChange,
                label = "Investimento (R$)",
                keyboardType = KeyboardType.Decimal,
                enabled = !ocupado
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.retornoFinanceiro,
                onValueChange = viewModel::onProjetoRetornoFinanceiroChange,
                label = "Retorno financeiro (R$)",
                keyboardType = KeyboardType.Decimal,
                enabled = !ocupado
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.reducaoCustos,
                onValueChange = viewModel::onProjetoReducaoCustosChange,
                label = "Redução de custos (R$)",
                keyboardType = KeyboardType.Decimal,
                enabled = !ocupado
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.ganhoProdutividade,
                onValueChange = viewModel::onProjetoGanhoProdutividadeChange,
                label = "Ganho de produtividade (%)",
                keyboardType = KeyboardType.Decimal,
                enabled = !ocupado
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.prazo,
                onValueChange = viewModel::onProjetoPrazoChange,
                label = "Prazo (AAAA-MM-DD)",
                enabled = !ocupado
            )

            if (state.erro != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.erro ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                text = when {
                    state.modoConversao -> "Confirmar conversão"
                    emEdicao -> "Salvar alterações"
                    else -> "Cadastrar projeto"
                },
                onClick = viewModel::salvarProjeto,
                loading = state.salvando,
                enabled = !ocupado,
                modifier = Modifier.testTag(TestTags.PROJETO_FORM_SALVAR)
            )

            if (emEdicao) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { confirmarExclusao = true },
                    enabled = !ocupado && !state.excluindo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.PROJETO_FORM_EXCLUIR)
                ) {
                    Text(text = "Excluir projeto")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            enabled = !ocupado,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF002B5C)
            )
        ) {
            Text(text = "Cancelar")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = { confirmarExclusao = false },
            title = { Text(text = "Excluir projeto?") },
            text = { Text(text = "Exclusão lógica com confirmação. Conflitos de versão exibirão erro 409.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarExclusao = false
                    viewModel.excluirProjeto()
                }) {
                    Text(text = "Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarExclusao = false }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

@Composable
private fun EstrategiaProjetoSelector(
    estrategias: List<com.fiap.inovagab.data.model.Orientacao>,
    selecionadaId: String,
    onSelecionar: (String, String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var aberto by remember { mutableStateOf(false) }
    val label = estrategias.find { it.id == selecionadaId }?.titulo ?: "Selecione a estratégia"
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { aberto = true }, enabled = enabled && estrategias.isNotEmpty()) {
            Text(text = "Estratégia: $label")
        }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            estrategias.filter { it.selecionavelParaNovoVinculo }.forEach { e ->
                DropdownMenuItem(
                    text = { Text(e.titulo) },
                    onClick = {
                        aberto = false
                        onSelecionar(e.id, e.titulo)
                    }
                )
            }
        }
    }
}

@Composable
private fun ResponsavelProjetoSelector(
    responsaveis: List<com.fiap.inovagab.data.remote.dto.ResponsavelResumoDto>,
    selecionadoId: String,
    onSelecionar: (String, String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var aberto by remember { mutableStateOf(false) }
    val label = responsaveis.find { it.id == selecionadoId }?.nome ?: "Selecione o responsável"
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { aberto = true }, enabled = enabled && responsaveis.isNotEmpty()) {
            Text(text = "Responsável: $label")
        }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            responsaveis.forEach { r ->
                DropdownMenuItem(
                    text = { Text(r.nome) },
                    onClick = {
                        aberto = false
                        onSelecionar(r.id, r.nome)
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusProjetoSelector(
    atual: StatusProjeto,
    onSelecionar: (StatusProjeto) -> Unit,
    enabled: Boolean
) {
    var aberto by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { aberto = true },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF002B5C)
            )
        ) {
            Text(text = "Status: ${formatarStatusProjeto(atual)}")
        }
        DropdownMenu(
            expanded = aberto,
            onDismissRequest = { aberto = false }
        ) {
            StatusProjeto.values().forEach { status ->
                DropdownMenuItem(
                    text = { Text(text = formatarStatusProjeto(status)) },
                    onClick = {
                        aberto = false
                        onSelecionar(status)
                    }
                )
            }
        }
    }
}

internal fun formatarStatusProjeto(status: StatusProjeto): String = when (status) {
    StatusProjeto.PLANEJADO -> "Planejado"
    StatusProjeto.EM_ANDAMENTO -> "Em andamento"
    StatusProjeto.CONCLUIDO -> "Concluído"
    StatusProjeto.CANCELADO -> "Cancelado"
}

internal fun corDoStatusProjeto(status: StatusProjeto): Color = when (status) {
    StatusProjeto.PLANEJADO -> Color(0xFF1565C0)
    StatusProjeto.EM_ANDAMENTO -> Color(0xFFB26A00)
    StatusProjeto.CONCLUIDO -> Color(0xFF1B7F3B)
    StatusProjeto.CANCELADO -> Color(0xFFB00020)
}
