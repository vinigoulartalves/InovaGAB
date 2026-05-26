package com.fiap.inovagab.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fiap.inovagab.data.model.User
import com.fiap.inovagab.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RankingUiState(
    val usuarios: List<User> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null
)

class RankingViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(RankingUiState())
    val state: StateFlow<RankingUiState> = _state.asStateFlow()

    fun carregar() {
        _state.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            repository.listarParaRanking().fold(
                onSuccess = { usuarios ->
                    _state.update {
                        it.copy(
                            carregando = false,
                            usuarios = usuarios,
                            erro = null
                        )
                    }
                },
                onFailure = { erro ->
                    _state.update {
                        it.copy(
                            carregando = false,
                            erro = erro.message ?: "Não foi possível carregar o ranking."
                        )
                    }
                }
            )
        }
    }
}
