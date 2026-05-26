package com.fiap.inovagab.ui.operador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fiap.inovagab.core.ui.components.AppButton
import com.fiap.inovagab.core.ui.components.AppTextField

@Composable
fun IdeiaFormScreen(
    onBack: () -> Unit = {},
    onSucesso: () -> Unit = {},
    viewModel: OperadorViewModel = viewModel()
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.limparFormulario()
    }

    LaunchedEffect(state.concluido) {
        if (state.concluido) {
            viewModel.consumirNavegacao()
            onSucesso()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Nova ideia",
            color = Color(0xFF002B5C),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Cadastre uma nova ideia ou problema para o gabinete.",
            color = Color(0xFF4A5A6E),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        AppTextField(
            value = state.titulo,
            onValueChange = viewModel::onTituloChange,
            label = "Título",
            enabled = !state.salvando
        )

        Spacer(modifier = Modifier.height(12.dp))

        AppTextField(
            value = state.area,
            onValueChange = viewModel::onAreaChange,
            label = "Área (ex.: Saúde, Educação, Infraestrutura)",
            enabled = !state.salvando
        )

        Spacer(modifier = Modifier.height(12.dp))

        AppTextField(
            value = state.descricao,
            onValueChange = viewModel::onDescricaoChange,
            label = "Descrição",
            singleLine = false,
            enabled = !state.salvando
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
            text = "Cadastrar ideia",
            onClick = viewModel::cadastrarIdeia,
            loading = state.salvando,
            enabled = !state.salvando
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}
