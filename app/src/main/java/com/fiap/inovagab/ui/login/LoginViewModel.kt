package com.fiap.inovagab.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fiap.inovagab.core.session.SessionManager
import com.fiap.inovagab.data.model.Perfil
import com.fiap.inovagab.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val loading: Boolean = false,
    val erro: String? = null,
    val perfilLogado: Perfil? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, erro = null) }
    }

    fun onSenhaChange(value: String) {
        _uiState.update { it.copy(senha = value, erro = null) }
    }

    fun onLoginClick() {
        val email = _uiState.value.email.trim()
        val senha = _uiState.value.senha

        if (email.isBlank() || senha.isBlank()) {
            _uiState.update { it.copy(erro = "Informe e-mail e senha.") }
            return
        }

        _uiState.update { it.copy(loading = true, erro = null) }

        viewModelScope.launch {
            val resultado = authRepository.login(email, senha)
            resultado.fold(
                onSuccess = { user ->
                    SessionManager.setUser(user)
                    _uiState.update {
                        it.copy(
                            loading = false,
                            erro = null,
                            perfilLogado = user.perfil
                        )
                    }
                },
                onFailure = { erro ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            erro = mensagemDeErro(erro)
                        )
                    }
                }
            )
        }
    }

    fun consumirNavegacao() {
        _uiState.update { it.copy(perfilLogado = null) }
    }

    fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    private fun mensagemDeErro(erro: Throwable): String = when (erro) {
        is FirebaseAuthInvalidUserException -> "Usuário não encontrado."
        is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha inválidos."
        else -> erro.message ?: "Não foi possível entrar. Tente novamente."
    }
}
