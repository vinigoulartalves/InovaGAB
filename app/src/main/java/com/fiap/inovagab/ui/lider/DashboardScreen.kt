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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fiap.inovagab.core.di.inovaViewModel
import com.fiap.inovagab.core.session.AppSession
import com.fiap.inovagab.core.testing.TestTags
import com.fiap.inovagab.core.ui.charts.GraficoDistribuicaoStatus
import com.fiap.inovagab.core.ui.charts.GraficoInvestimentoRetorno
import com.fiap.inovagab.core.ui.components.AppButton
import com.fiap.inovagab.core.ui.components.AppCard
import com.fiap.inovagab.core.ui.components.AppTextField
import com.fiap.inovagab.core.ui.effects.OnResumeEffect
import com.fiap.inovagab.core.ui.format.formatarMoedaPtBr
import com.fiap.inovagab.core.ui.format.formatarPercentualOpcionalPtBr
import com.fiap.inovagab.core.ui.format.formatarRoiPtBr
import com.fiap.inovagab.data.model.DashboardReport
import com.fiap.inovagab.data.model.Perfil

@Composable
fun DashboardScreen(
    onBack: () -> Unit = {},
    viewModel: LiderViewModel = inovaViewModel()
) {
    val usuario by AppSession.manager.currentUser.collectAsState()
    val perfil = usuario?.perfil
    val state by viewModel.dashboardState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.carregarOpcoesDashboard()
    }

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
            text = "Relatórios consolidados do backend com filtros por estratégia, projeto e período.",
            color = Color(0xFF4A5A6E),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            perfil != null && perfil != Perfil.LIDER -> {
                AcessoRestritoCard()
            }

            else -> {
                FiltrosDashboard(state = state, viewModel = viewModel)

                Spacer(modifier = Modifier.height(16.dp))

                when {
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

                    state.vazio && state.report != null -> {
                        AppCard {
                            Text(
                                text = "Nenhum dado para os filtros selecionados.",
                                color = Color(0xFF4A5A6E),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    state.report != null -> {
                        DashboardConteudo(report = state.report!!)
                    }
                }
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
private fun FiltrosDashboard(state: DashboardUiState, viewModel: LiderViewModel) {
    AppCard {
        Text(
            text = "Filtros",
            color = Color(0xFF002B5C),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))

        FiltroDropdown(
            label = "Estratégia",
            opcoes = listOf(null to "Todas") + state.estrategiasOpcoes.map { it.id to it.titulo },
            selecionado = state.filtros.estrategiaId,
            onSelecionar = viewModel::onFiltroEstrategiaChange,
            testTag = TestTags.DASHBOARD_FILTRO_ESTRATEGIA
        )

        Spacer(modifier = Modifier.height(8.dp))

        FiltroDropdown(
            label = "Projeto",
            opcoes = listOf(null to "Todos") + state.projetosOpcoes.map { it.id to it.nome },
            selecionado = state.filtros.projetoId,
            onSelecionar = viewModel::onFiltroProjetoChange,
            testTag = TestTags.DASHBOARD_FILTRO_PROJETO
        )

        Spacer(modifier = Modifier.height(8.dp))

        AppTextField(
            value = state.filtros.inicio ?: "",
            onValueChange = viewModel::onFiltroInicioChange,
            label = "Criação a partir de (AAAA-MM-DD)",
            modifier = Modifier.testTag(TestTags.DASHBOARD_FILTRO_INICIO)
        )

        Spacer(modifier = Modifier.height(8.dp))

        AppTextField(
            value = state.filtros.fim ?: "",
            onValueChange = viewModel::onFiltroFimChange,
            label = "Criação até (AAAA-MM-DD)",
            modifier = Modifier.testTag(TestTags.DASHBOARD_FILTRO_FIM)
        )

        Spacer(modifier = Modifier.height(12.dp))

        AppButton(
            text = "Aplicar filtros",
            onClick = { viewModel.carregarDashboard() },
            modifier = Modifier.testTag(TestTags.DASHBOARD_APLICAR_FILTROS)
        )
    }
}

@Composable
private fun FiltroDropdown(
    label: String,
    opcoes: List<Pair<String?, String>>,
    selecionado: String?,
    onSelecionar: (String?) -> Unit,
    testTag: String
) {
    var aberto by remember { mutableStateOf(false) }
    val textoAtual = opcoes.find { it.first == selecionado }?.second ?: label

    Box(modifier = Modifier.testTag(testTag)) {
        OutlinedButton(onClick = { aberto = true }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "$label: $textoAtual")
        }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            opcoes.forEach { (id, nome) ->
                DropdownMenuItem(
                    text = { Text(nome) },
                    onClick = {
                        aberto = false
                        onSelecionar(id)
                    }
                )
            }
        }
    }
}

@Composable
private fun DashboardConteudo(report: DashboardReport) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IndicadorCard("Total de projetos", report.totalProjetos.toString())
        IndicadorCard("Investimento total", formatarMoedaPtBr(report.investimentoTotal))
        IndicadorCard("Retorno total", formatarMoedaPtBr(report.retornoTotal))
        IndicadorCard(
            "Lucro obtido",
            formatarMoedaPtBr(report.lucroObtido),
            if (report.lucroObtido >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
        )
        IndicadorCard(
            "ROI geral",
            formatarRoiPtBr(report.roiGeral),
            if ((report.roiGeral ?: 0.0) >= 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
        )
        IndicadorCard("Redução de custos", formatarMoedaPtBr(report.reducaoCustosTotal))
        IndicadorCard(
            "Ganho médio de produtividade",
            formatarPercentualOpcionalPtBr(report.ganhoProdutividadeMedio)
        )
        IndicadorCard("Projetos atrasados", report.projetosAtrasados.toString())

        AppCard {
            Text(
                text = "Investimento x retorno por estratégia",
                color = Color(0xFF002B5C),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
            GraficoInvestimentoRetorno(
                series = report.investimentoRetornoPorEstrategia,
                modifier = Modifier.testTag(TestTags.DASHBOARD_GRAFICO_INVEST_RETORNO)
            )
        }

        AppCard {
            Text(
                text = "Distribuição por status",
                color = Color(0xFF002B5C),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
            GraficoDistribuicaoStatus(
                distribuicao = report.distribuicaoPorStatus,
                modifier = Modifier.testTag(TestTags.DASHBOARD_GRAFICO_STATUS)
            )
        }
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
