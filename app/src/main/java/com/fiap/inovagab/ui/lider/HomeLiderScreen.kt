package com.fiap.inovagab.ui.lider

import androidx.compose.runtime.Composable
import com.fiap.inovagab.core.ui.components.PlaceholderScreen

@Composable
fun HomeLiderScreen(onLogout: () -> Unit = {}) {
    PlaceholderScreen(titulo = "Home Líder", onLogout = onLogout)
}
