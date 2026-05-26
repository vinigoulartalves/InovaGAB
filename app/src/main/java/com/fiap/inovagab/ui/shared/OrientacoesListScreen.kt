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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.fiap.inovagab.core.session.SessionManager
import com.fiap.inovagab.core.ui.components.AppCard
import com.fiap.inovagab.data.model.Orientacao
import com.fiap.inovagab.data.model.Perfil
import com.fiap.inovagab.ui.lider.LiderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrientacoesListScreen(
    onBack: () -> Unit = {},
    onCriar: () -> Unit = {},
    onEditar: (String) -> Unit = {},
    viewModel: LiderViewModel = viewModel()
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()
    val usuario by SessionManager.currentUser.collectAsState()
    val isLider = usuario?.perfil == Perfil.LIDER

    LaunchedEffect(Unit) {
        viewModel.carregarOrientacoes()
    }

    var orientacaoParaExcluir by remember { mutableStateOf<Orientacao?>(null) }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        floatingActionButton = {
            if (isLider) {
                ExtendedFloatingActionButton(
                    onClick = onCriar,
                    containerColor = Color(0xFF002B5C),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Nova orientação")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Orientações Estratégicas",
                color = Color(0xFF002B5C),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isLider) {
                    "Crie, edite e mantenha as orientações da liderança."
                } else {
                    "Acompanhe as orientações estratégicas da liderança."
                },
                color = Color(0xFF4A5A6E),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                state.loading && state.orientacoes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF002B5C))
                    }
                }

                state.erro != null && state.orientacoes.isEmpty() -> {
                    MensagemErroComRetry(
                        mensagem = state.erro ?: "",
                        onRetry = { viewModel.carregarOrientacoes() }
                    )
                }

                state.orientacoes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Nenhuma orientação cadastrada ainda.",
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
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.orientacoes, key = { it.id }) { orientacao ->
                            OrientacaoCard(
                                orientacao = orientacao,
                                isLider = isLider,
                                onEditar = { onEditar(orientacao.id) },
                                onExcluir = { orientacaoParaExcluir = orientacao }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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

    val alvo = orientacaoParaExcluir
    if (alvo != null) {
        AlertDialog(
            onDismissRequest = { orientacaoParaExcluir = null },
            title = { Text(text = "Excluir orientação") },
            text = {
                Text(text = "Deseja realmente excluir \"${alvo.titulo}\"? Esta ação não pode ser desfeita.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.excluir(alvo.id)
                    orientacaoParaExcluir = null
                }) {
                    Text(text = "Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { orientacaoParaExcluir = null }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

@Composable
private fun OrientacaoCard(
    orientacao: Orientacao,
    isLider: Boolean,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    AppCard {
        Text(
            text = orientacao.titulo,
            color = Color(0xFF002B5C),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = orientacao.descricao,
            color = Color(0xFF4A5A6E),
            style = MaterialTheme.typography.bodyMedium
        )

        if (orientacao.criadoEm > 0L) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Criada em ${formatarData(orientacao.criadoEm)}",
                color = Color(0xFF7A8AA0),
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (isLider) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditar) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Editar",
                        tint = Color(0xFF002B5C)
                    )
                }
                IconButton(onClick = onExcluir) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun MensagemErroComRetry(mensagem: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = mensagem,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onRetry) {
            Text(text = "Tentar novamente")
        }
    }
}

private fun formatarData(timestamp: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return formato.format(Date(timestamp))
}
