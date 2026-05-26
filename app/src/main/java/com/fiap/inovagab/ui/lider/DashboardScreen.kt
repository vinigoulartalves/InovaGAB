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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.fiap.inovagab.core.ui.effects.OnResumeEffect
import com.fiap.inovagab.data.model.Perfil
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    onBack: () -> Unit = {},
    viewModel: LiderViewModel = viewModel()
) {
    val usuario by SessionManager.currentUser.collectAsState()
    val perfil = usuario?.perfil
    val state by viewModel.dashboardState.collectAsStateWithLifecycle()

    OnResumeEffect {
        if (perfil == Perfil.LIDER) {
            viewModel.carregarDashboard()
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
            text = "Dashboard de Inovação",
            color = Color(0xFF002B5C),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Visão consolidada dos projetos cadastrados.",
            color = Color(0xFF4A5A6E),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            perfil != null && perfil != Perfil.LIDER -> {
                AcessoRestritoCard()
            }

            state.carregando -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF002B5C))
                }
            }

            state.erro != null -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.erro ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.carregarDashboard() }) {
                        Text(text = "Tentar novamente")
                    }
                }
            }

            else -> {
                DashboardCards(state = state)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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

@Composable
private fun DashboardCards(state: DashboardUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IndicadorCard(
            titulo = "Total de projetos",
            valor = state.totalProjetos.toString()
        )
        IndicadorCard(
            titulo = "Investimento total",
            valor = formatarMoeda(state.investimentoTotal)
        )
        IndicadorCard(
            titulo = "Retorno total",
            valor = formatarMoeda(state.retornoTotal)
        )
        IndicadorCard(
            titulo = "Lucro obtido",
            valor = formatarMoeda(state.lucroObtido),
            corValor = if (state.lucroObtido >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
        )
        IndicadorCard(
            titulo = "ROI geral",
            valor = formatarPercentual(state.roiGeral),
            corValor = if (state.roiGeral >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
        )
        IndicadorCard(
            titulo = "Redução de custos",
            valor = formatarMoeda(state.reducaoCustosTotal)
        )
        IndicadorCard(
            titulo = "Ganho médio de produtividade",
            valor = formatarPercentual(state.ganhoProdutividadeMedio)
        )
    }
}

@Composable
private fun IndicadorCard(
    titulo: String,
    valor: String,
    corValor: Color = Color(0xFF002B5C)
) {
    AppCard {
        Text(
            text = titulo,
            color = Color(0xFF4A5A6E),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = valor,
            color = corValor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AcessoRestritoCard() {
    AppCard {
        Text(
            text = "Acesso restrito",
            color = Color(0xFF002B5C),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Apenas usuários com perfil de Liderança podem acessar esta dashboard.",
            color = Color(0xFF4A5A6E),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatarMoeda(valor: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return formato.format(valor)
}

private fun formatarPercentual(valor: Double): String {
    val formato = NumberFormat.getNumberInstance(Locale("pt", "BR"))
    formato.maximumFractionDigits = 2
    formato.minimumFractionDigits = 0
    return "${formato.format(valor)}%"
}
