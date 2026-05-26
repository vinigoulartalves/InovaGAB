package com.fiap.inovagab.ui.lider

import androidx.compose.runtime.Composable
import com.fiap.inovagab.core.ui.components.PlaceholderScreen

@Composable
fun DashboardScreen(onBack: () -> Unit = {}) {
    PlaceholderScreen(titulo = "Dashboard de Inovação", onBack = onBack)
}
