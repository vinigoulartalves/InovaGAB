package com.fiap.inovagab.ui.operador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fiap.inovagab.core.session.SessionManager
import com.fiap.inovagab.data.model.Ideia
import com.fiap.inovagab.data.model.PrioridadeIdeia
import com.fiap.inovagab.data.model.StatusIdeia
import com.fiap.inovagab.data.repository.IdeiaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IdeiaFormUiState(
    val titulo: String = "",
    val area: String = "",
    val descricao: String = "",
    val salvando: Boolean = false,
    val erro: String? = null,
    val sucesso: String? = null,
    val concluido: Boolean = false
)

data class MinhasIdeiasUiState(
    val ideias: List<Ideia> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null
)

class OperadorViewModel(
    private val repository: IdeiaRepository = IdeiaRepository()
) : ViewModel() {

    private val _formState = MutableStateFlow(IdeiaFormUiState())
    val formState: StateFlow<IdeiaFormUiState> = _formState.asStateFlow()

    private val _listState = MutableStateFlow(MinhasIdeiasUiState())
    val listState: StateFlow<MinhasIdeiasUiState> = _listState.asStateFlow()

    fun onTituloChange(value: String) {
        _formState.update { it.copy(titulo = value, erro = null, sucesso = null) }
    }

    fun onAreaChange(value: String) {
        _formState.update { it.copy(area = value, erro = null, sucesso = null) }
    }

    fun onDescricaoChange(value: String) {
        _formState.update { it.copy(descricao = value, erro = null, sucesso = null) }
    }

    fun limparFormulario() {
        _formState.value = IdeiaFormUiState()
    }

    fun consumirNavegacao() {
        _formState.update { it.copy(concluido = false) }
    }

    fun cadastrarIdeia() {
        val state = _formState.value
        val titulo = state.titulo.trim()
        val area = state.area.trim()
        val descricao = state.descricao.trim()

        if (titulo.isBlank() || area.isBlank() || descricao.isBlank()) {
            _formState.update {
                it.copy(
                    erro = "Preencha título, área e descrição.",
                    sucesso = null
                )
            }
            return
        }

        val usuario = SessionManager.currentUser.value
        if (usuario == null || usuario.uid.isBlank()) {
            _formState.update {
                it.copy(
                    erro = "Sessão expirada. Faça login novamente.",
                    sucesso = null
                )
            }
            return
        }

        _formState.update { it.copy(salvando = true, erro = null, sucesso = null) }

        viewModelScope.launch {
            val ideia = Ideia(
                titulo = titulo,
                area = area,
                descricao = descricao,
                autorId = usuario.uid,
                autorNome = usuario.nome,
                status = StatusIdeia.ENVIADA,
                prioridade = PrioridadeIdeia.MEDIA
            )

            repository.criar(ideia).fold(
                onSuccess = {
                    _formState.update {
                        IdeiaFormUiState(
                            sucesso = "Ideia cadastrada com sucesso! Você ganhou 10 pontos.",
                            concluido = true
                        )
                    }
                },
                onFailure = { erro ->
                    _formState.update {
                        it.copy(
                            salvando = false,
                            erro = erro.message ?: "Não foi possível cadastrar a ideia."
                        )
                    }
                }
            )
        }
    }

    fun carregarMinhasIdeias() {
        val usuario = SessionManager.currentUser.value
        if (usuario == null || usuario.uid.isBlank()) {
            _listState.update {
                it.copy(
                    carregando = false,
                    erro = "Sessão expirada. Faça login novamente.",
                    ideias = emptyList()
                )
            }
            return
        }

        _listState.update { it.copy(carregando = true, erro = null) }

        viewModelScope.launch {
            repository.listarPorAutor(usuario.uid).fold(
                onSuccess = { ideias ->
                    _listState.update {
                        it.copy(carregando = false, ideias = ideias, erro = null)
                    }
                },
                onFailure = { erro ->
                    _listState.update {
                        it.copy(
                            carregando = false,
                            erro = erro.message ?: "Não foi possível carregar suas ideias."
                        )
                    }
                }
            )
        }
    }
}
