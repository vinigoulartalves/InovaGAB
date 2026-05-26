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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fiap.inovagab.core.session.SessionManager
import com.fiap.inovagab.core.ui.components.AppCard
import com.fiap.inovagab.data.model.Perfil
import com.fiap.inovagab.data.model.Projeto
import com.fiap.inovagab.data.model.StatusProjeto
import com.fiap.inovagab.ui.gestor.GestorViewModel
import com.fiap.inovagab.ui.gestor.corDoStatusProjeto
import com.fiap.inovagab.ui.gestor.formatarStatusProjeto
import com.fiap.inovagab.ui.lider.LiderViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjetosListScreen(
    onBack: () -> Unit = {},
    onCriar: () -> Unit = {},
    onEditar: (String) -> Unit = {},
    gestorViewModel: GestorViewModel = viewModel(),
    liderViewModel: LiderViewModel = viewModel()
) {
    val usuario by SessionManager.currentUser.collectAsState()
    val perfil = usuario?.perfil
    val isGestor = perfil == Perfil.GESTOR
    val isLider = perfil == Perfil.LIDER

    val gestorState by gestorViewModel.projetosListState.collectAsStateWithLifecycle()
    val liderState by liderViewModel.projetosState.collectAsStateWithLifecycle()

    val projetos = if (isLider) liderState.projetos else gestorState.projetos
    val carregando = if (isLider) liderState.carregando else gestorState.carregando
    val erro = if (isLider) liderState.erro else gestorState.erro

    LaunchedEffect(perfil) {
        if (isLider) {
            liderViewModel.consultarProjetos()
        } else {
            gestorViewModel.carregarProjetos()
        }
    }

    val recarregar: () -> Unit = {
        if (isLider) liderViewModel.consultarProjetos() else gestorViewModel.carregarProjetos()
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        floatingActionButton = {
            if (isGestor) {
                ExtendedFloatingActionButton(
                    onClick = onCriar,
                    containerColor = Color(0xFF002B5C),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Novo projeto")
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
                text = "Projetos",
                color = Color(0xFF002B5C),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when {
                    isGestor -> "Cadastre, edite e acompanhe os projetos do gabinete."
                    isLider -> "Consulte os projetos e iniciativas em andamento."
                    else -> "Acompanhe os projetos do gabinete."
                },
                color = Color(0xFF4A5A6E),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                carregando && projetos.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF002B5C))
                    }
                }

                erro != null && projetos.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = erro,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(onClick = recarregar) {
                            Text(text = "Tentar novamente")
                        }
                    }
                }

                projetos.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum projeto cadastrado ainda.",
                            color = Color(0xFF4A5A6E),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                else -> {
                    if (erro != null) {
                        Text(
                            text = erro,
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
                        items(projetos, key = { it.id }) { projeto ->
                            ProjetoCard(
                                projeto = projeto,
                                podeEditar = isGestor,
                                onEditar = { onEditar(projeto.id) }
                            )
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
private fun ProjetoCard(
    projeto: Projeto,
    podeEditar: Boolean,
    onEditar: () -> Unit
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = projeto.nome,
                    color = Color(0xFF002B5C),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (projeto.descricao.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = projeto.descricao,
                        color = Color(0xFF2C3E50),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (podeEditar) {
                IconButton(onClick = onEditar) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Editar projeto",
                        tint = Color(0xFF002B5C)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EtiquetaProjeto(
                texto = formatarStatusProjeto(projeto.status),
                cor = corDoStatusProjeto(projeto.status)
            )
            if (projeto.etapa.isNotBlank()) {
                EtiquetaProjeto(
                    texto = "Etapa: ${projeto.etapa}",
                    cor = Color(0xFF4A5A6E)
                )
            }
        }

        if (projeto.responsavel.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Responsável: ${projeto.responsavel}",
                color = Color(0xFF4A5A6E),
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (projeto.prazo.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Prazo: ${projeto.prazo}",
                color = Color(0xFF4A5A6E),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        ValorLinha(label = "Investimento", valor = formatarMoeda(projeto.investimento))
        ValorLinha(label = "Retorno financeiro", valor = formatarMoeda(projeto.retornoFinanceiro))
        ValorLinha(label = "Redução de custos", valor = formatarMoeda(projeto.reducaoCustos))
        ValorLinha(
            label = "Ganho de produtividade",
            valor = formatarPercentual(projeto.ganhoProdutividade)
        )

        if (projeto.criadoEm > 0L) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cadastrado em ${formatarData(projeto.criadoEm)}",
                color = Color(0xFF7A8AA0),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ValorLinha(label: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0xFF4A5A6E),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = valor,
            color = Color(0xFF2C3E50),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EtiquetaProjeto(texto: String, cor: Color) {
    Surface(
        color = cor.copy(alpha = 0.15f),
        contentColor = cor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Suppress("unused")
private fun statusDescritivo(status: StatusProjeto): String = formatarStatusProjeto(status)

private fun formatarMoeda(valor: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return formato.format(valor)
}

private fun formatarPercentual(valor: Double): String {
    val formato = NumberFormat.getNumberInstance(Locale("pt", "BR"))
    formato.maximumFractionDigits = 2
    return "${formato.format(valor)}%"
}

private fun formatarData(timestamp: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return formato.format(Date(timestamp))
}
