package com.fiap.inovagab.ui.shared

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fiap.inovagab.core.ui.effects.OnResumeEffect
import com.fiap.inovagab.data.model.Perfil
import com.fiap.inovagab.data.model.User

@Composable
fun RankingScreen(
    onBack: () -> Unit = {},
    viewModel: RankingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OnResumeEffect {
        viewModel.carregar()
    }

    Scaffold(containerColor = Color(0xFFF5F7FA)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Ranking de Inovação",
                color = Color(0xFF002B5C),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Classificação dos colaboradores por pontos acumulados.",
                color = Color(0xFF4A5A6E),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                state.carregando && state.usuarios.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF002B5C))
                    }
                }

                state.erro != null && state.usuarios.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.erro ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = { viewModel.carregar() }) {
                            Text(text = "Tentar novamente")
                        }
                    }
                }

                state.usuarios.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum usuário cadastrado ainda.",
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
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        itemsIndexed(
                            state.usuarios,
                            key = { _, user -> user.uid.ifBlank { user.email } }
                        ) { index, user ->
                            RankingCard(posicao = index + 1, usuario = user)
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
}

@Composable
private fun RankingCard(posicao: Int, usuario: User) {
    val destaque = corDoDestaque(posicao)
    val rotuloPodio = rotuloPodio(posicao)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color = destaque, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = posicao.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (rotuloPodio != null) {
                    Text(
                        text = rotuloPodio,
                        color = destaque,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = usuario.nome.ifBlank { "Sem nome" },
                    color = Color(0xFF002B5C),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                EtiquetaPerfil(perfil = usuario.perfil)

                if (usuario.email.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = usuario.email,
                        color = Color(0xFF4A5A6E),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = usuario.pontos.toString(),
                    color = Color(0xFF002B5C),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (usuario.pontos == 1) "ponto" else "pontos",
                    color = Color(0xFF4A5A6E),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun EtiquetaPerfil(perfil: Perfil) {
    val cor = Color(0xFF4A5A6E)
    Surface(
        color = cor.copy(alpha = 0.12f),
        contentColor = cor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = formatarPerfil(perfil),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private fun formatarPerfil(perfil: Perfil): String = when (perfil) {
    Perfil.OPERADOR -> "Operador"
    Perfil.GESTOR -> "Gestor"
    Perfil.LIDER -> "Liderança"
}

private fun rotuloPodio(posicao: Int): String? = when (posicao) {
    1 -> "1º lugar"
    2 -> "2º lugar"
    3 -> "3º lugar"
    else -> null
}

private fun corDoDestaque(posicao: Int): Color = when (posicao) {
    1 -> Color(0xFFD4AF37)
    2 -> Color(0xFF9AA5B1)
    3 -> Color(0xFFB87333)
    else -> Color(0xFF002B5C)
}
