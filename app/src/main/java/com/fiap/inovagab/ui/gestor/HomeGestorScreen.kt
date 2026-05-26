package com.fiap.inovagab.ui.gestor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fiap.inovagab.core.session.SessionManager
import com.fiap.inovagab.core.ui.components.AppButton
import com.fiap.inovagab.core.ui.components.HomeMenuButton

@Composable
fun HomeGestorScreen(
    onVerOrientacoes: () -> Unit = {},
    onGerenciarIdeias: () -> Unit = {},
    onCadastrarProjeto: () -> Unit = {},
    onProjetos: () -> Unit = {},
    onRanking: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val usuario by SessionManager.currentUser.collectAsState()
    val nome = usuario?.nome.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "InovaGAB",
            color = Color(0xFF002B5C),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Perfil: Gestor",
            color = Color(0xFF4A5A6E),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        if (nome.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Olá, $nome",
                color = Color(0xFF4A5A6E),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeMenuButton(text = "Ver orientações", onClick = onVerOrientacoes)
            HomeMenuButton(text = "Gerenciar ideias", onClick = onGerenciarIdeias)
            HomeMenuButton(text = "Cadastrar projeto", onClick = onCadastrarProjeto)
            HomeMenuButton(text = "Projetos", onClick = onProjetos)
            HomeMenuButton(text = "Ranking", onClick = onRanking)
        }

        Spacer(modifier = Modifier.height(32.dp))

        AppButton(
            text = "Sair",
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
