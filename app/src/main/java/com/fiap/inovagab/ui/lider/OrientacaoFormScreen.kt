package com.fiap.inovagab.ui.lider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fiap.inovagab.core.ui.components.AppButton
import com.fiap.inovagab.core.ui.components.AppTextField

@Composable
fun OrientacaoFormScreen(
    orientacaoId: String? = null,
    onBack: () -> Unit = {},
    onSucesso: () -> Unit = {},
    viewModel: LiderViewModel = viewModel()
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()

    LaunchedEffect(orientacaoId) {
        viewModel.iniciarFormulario(orientacaoId)
    }

    LaunchedEffect(state.concluido) {
        if (state.concluido) {
            viewModel.consumirNavegacao()
            onSucesso()
        }
    }

    var confirmarExclusao by remember { mutableStateOf(false) }
    val emEdicao = state.isEdicao
    val ocupado = state.salvando || state.excluindo || state.carregando

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = if (emEdicao) "Editar orientação" else "Nova orientação",
            color = Color(0xFF002B5C),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Defina o título e a descrição da orientação estratégica.",
            color = Color(0xFF4A5A6E),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (state.carregando) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF002B5C))
            }
        } else {
            AppTextField(
                value = state.titulo,
                onValueChange = viewModel::onTituloChange,
                label = "Título",
                enabled = !ocupado
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.descricao,
                onValueChange = viewModel::onDescricaoChange,
                label = "Descrição",
                singleLine = false,
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
                text = if (emEdicao) "Salvar alterações" else "Cadastrar orientação",
                onClick = viewModel::salvar,
                loading = state.salvando,
                enabled = !ocupado
            )

            if (emEdicao) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { confirmarExclusao = true },
                    enabled = !ocupado,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = if (state.excluindo) "Excluindo..." else "Excluir orientação",
                        style = MaterialTheme.typography.titleMedium
                    )
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
            title = { Text(text = "Excluir orientação") },
            text = { Text(text = "Tem certeza que deseja excluir esta orientação?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarExclusao = false
                    viewModel.excluir(state.id)
                }) {
                    Text(text = "Excluir", color = MaterialTheme.colorScheme.error)
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
