package com.fiap.inovagab.ui.shared

import androidx.compose.runtime.Composable
import com.fiap.inovagab.core.ui.components.PlaceholderScreen

@Composable
fun ProjetosListScreen(onBack: () -> Unit = {}) {
    PlaceholderScreen(titulo = "Projetos", onBack = onBack)
}
