package com.fiap.inovagab.ui.gestor

import androidx.compose.runtime.Composable
import com.fiap.inovagab.core.ui.components.PlaceholderScreen

@Composable
fun HomeGestorScreen(onLogout: () -> Unit = {}) {
    PlaceholderScreen(titulo = "Home Gestor", onLogout = onLogout)
}
