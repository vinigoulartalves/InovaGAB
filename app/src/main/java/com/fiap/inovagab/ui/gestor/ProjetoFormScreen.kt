package com.fiap.inovagab.ui.gestor

import androidx.compose.runtime.Composable
import com.fiap.inovagab.core.ui.components.PlaceholderScreen

@Composable
fun ProjetoFormScreen(onBack: () -> Unit = {}) {
    PlaceholderScreen(titulo = "Novo Projeto", onBack = onBack)
}
