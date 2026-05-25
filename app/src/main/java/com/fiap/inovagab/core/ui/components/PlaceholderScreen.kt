package com.fiap.inovagab.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

@Composable
fun PlaceholderScreen(
    titulo: String,
    descricao: String = "Em construção. Esta tela será implementada nas próximas etapas do Challenge.",
    onLogout: (() -> Unit)? = null
) {
    val usuario by SessionManager.currentUser.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titulo,
                color = Color(0xFF002B5C),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (usuario != null) {
                Text(
                    text = "Olá, ${usuario?.nome.orEmpty()}",
                    color = Color(0xFF002B5C),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = descricao,
                color = Color(0xFF4A5A6E),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            if (onLogout != null) {
                Spacer(modifier = Modifier.height(24.dp))
                AppButton(
                    text = "Sair",
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
