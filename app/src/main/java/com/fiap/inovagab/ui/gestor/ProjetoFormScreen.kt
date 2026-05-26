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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fiap.inovagab.core.ui.components.AppButton
import com.fiap.inovagab.core.ui.components.AppTextField
import com.fiap.inovagab.data.model.StatusProjeto

@Composable
fun ProjetoFormScreen(
    projetoId: String? = null,
    onBack: () -> Unit = {},
    onSucesso: () -> Unit = {},
    viewModel: GestorViewModel = viewModel()
) {
    val state by viewModel.projetoFormState.collectAsStateWithLifecycle()

    LaunchedEffect(projetoId) {
        viewModel.iniciarFormularioProjeto(projetoId)
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
            text = if (emEdicao) "Editar projeto" else "Novo projeto",
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

            AppTextField(
                value = state.responsavel,
                onValueChange = viewModel::onProjetoResponsavelChange,
                label = "Responsável",
                enabled = !ocupado
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
                label = "Prazo (ex.: 31/12/2026)",
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
                text = if (emEdicao) "Salvar alterações" else "Cadastrar projeto",
                onClick = viewModel::salvarProjeto,
                loading = state.salvando,
                enabled = !ocupado
            )
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
