package com.fiap.inovagab.ui.login

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fiap.inovagab.core.ui.components.AppButton
import com.fiap.inovagab.core.ui.components.AppTextField
import com.fiap.inovagab.data.model.Perfil

@Composable
fun LoginScreen(
    onLoginSucesso: (Perfil) -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.perfilLogado) {
        val perfil = state.perfilLogado
        if (perfil != null) {
            viewModel.consumirNavegacao()
            onLoginSucesso(perfil)
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF002B5C),
            Color(0xFF1E4E8C)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "InovaGAB",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Inovação que move o Grupo Águia Branca",
                color = Color(0xFFE6E6E6),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(48.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Entrar",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF002B5C),
                        fontWeight = FontWeight.SemiBold
                    )

                    AppTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "E-mail corporativo",
                        enabled = !state.loading
                    )

                    AppTextField(
                        value = state.senha,
                        onValueChange = viewModel::onSenhaChange,
                        label = "Senha",
                        isPassword = true,
                        enabled = !state.loading
                    )

                    if (state.erro != null) {
                        Text(
                            text = state.erro ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    AppButton(
                        text = "Entrar",
                        onClick = viewModel::onLoginClick,
                        loading = state.loading
                    )

                    TextButton(
                        onClick = { /* TODO: navegação para cadastro futura */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Não tem conta? Fale com o RH",
                            color = Color(0xFF002B5C)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Challenge FIAP • Grupo Águia Branca",
                color = Color(0xFFC9A24B),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 24.dp, top = 32.dp)
            )
        }
    }
}
