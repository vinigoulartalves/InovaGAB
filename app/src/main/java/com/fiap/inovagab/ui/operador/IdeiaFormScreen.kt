package com.fiap.inovagab.ui.operador

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fiap.inovagab.core.di.inovaViewModel
import com.fiap.inovagab.core.testing.TestTags
import com.fiap.inovagab.core.ui.components.AppButton
import com.fiap.inovagab.core.ui.components.AppTextField

@Composable
fun IdeiaFormScreen(
    ideiaId: String? = null,
    onBack: () -> Unit = {},
    onSucesso: () -> Unit = {},
    viewModel: OperadorViewModel = inovaViewModel()
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()

    LaunchedEffect(ideiaId) {
        viewModel.iniciarFormulario(ideiaId)
    }

    LaunchedEffect(state.concluido) {
        if (state.concluido) {
            viewModel.consumirNavegacao()
            onSucesso()
        }
    }

    val emEdicao = state.isEdicao
    var estrategiaMenuAberto by remember { mutableStateOf(false) }
    val estrategiaLabel = state.estrategias.find { it.id == state.estrategiaId }?.titulo
        ?: "Selecione a estratégia vigente"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = if (emEdicao) "Editar ideia" else "Nova ideia",
            color = Color(0xFF002B5C),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Vincule à estratégia vigente e descreva a ideia ou problema.",
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
            Box(modifier = Modifier.testTag(TestTags.IDEIA_FORM_ESTRATEGIA)) {
                OutlinedButton(
                    onClick = { estrategiaMenuAberto = true },
                    enabled = !state.salvando && state.estrategias.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Estratégia: $estrategiaLabel")
                }
                DropdownMenu(
                    expanded = estrategiaMenuAberto,
                    onDismissRequest = { estrategiaMenuAberto = false }
                ) {
                    state.estrategias.forEach { estrategia ->
                        DropdownMenuItem(
                            text = { Text(estrategia.titulo) },
                            onClick = {
                                estrategiaMenuAberto = false
                                viewModel.onEstrategiaChange(estrategia.id)
                            }
                        )
                    }
                }
            }

            if (state.estrategias.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nenhuma estratégia vigente disponível no momento.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.titulo,
                onValueChange = viewModel::onTituloChange,
                label = "Título",
                enabled = !state.salvando,
                modifier = Modifier.testTag(TestTags.IDEIA_FORM_TITULO)
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.area,
                onValueChange = viewModel::onAreaChange,
                label = "Área (ex.: Saúde, Educação, Infraestrutura)",
                enabled = !state.salvando,
                modifier = Modifier.testTag(TestTags.IDEIA_FORM_AREA)
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppTextField(
                value = state.descricao,
                onValueChange = viewModel::onDescricaoChange,
                label = "Descrição",
                singleLine = false,
                enabled = !state.salvando,
                modifier = Modifier.testTag(TestTags.IDEIA_FORM_DESCRICAO)
            )

            if (state.erro != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.erro ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (state.sucesso != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.sucesso ?: "",
                    color = Color(0xFF1B7F3B),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                text = if (emEdicao) "Salvar alterações" else "Cadastrar ideia",
                onClick = viewModel::salvarIdeia,
                loading = state.salvando,
                enabled = !state.salvando,
                modifier = Modifier.testTag(TestTags.IDEIA_FORM_SALVAR)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                enabled = !state.salvando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF002B5C)
                )
            ) {
                Text(text = "Cancelar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
