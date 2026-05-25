package com.fiap.inovagab.ui.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val loading: Boolean = false,
    val erro: String? = null,
    val loginConcluido: Boolean = false
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, erro = null) }
    }

    fun onSenhaChange(value: String) {
        _uiState.update { it.copy(senha = value, erro = null) }
    }

    fun onLoginClick() {
        // Lógica real de autenticação será implementada futuramente.
        _uiState.update { it.copy(erro = "Autenticação ainda não implementada.") }
    }

    fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }
}
