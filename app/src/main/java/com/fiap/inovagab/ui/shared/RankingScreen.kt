package com.fiap.inovagab.ui.shared

import androidx.compose.runtime.Composable
import com.fiap.inovagab.core.ui.components.PlaceholderScreen

@Composable
fun RankingScreen(onBack: () -> Unit = {}) {
    PlaceholderScreen(titulo = "Ranking de Inovação", onBack = onBack)
}
