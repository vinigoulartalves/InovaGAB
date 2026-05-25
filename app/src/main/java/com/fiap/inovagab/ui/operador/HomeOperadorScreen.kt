package com.fiap.inovagab.ui.operador

import androidx.compose.runtime.Composable
import com.fiap.inovagab.core.ui.components.PlaceholderScreen

@Composable
fun HomeOperadorScreen(onLogout: () -> Unit = {}) {
    PlaceholderScreen(titulo = "Home Operador", onLogout = onLogout)
}
