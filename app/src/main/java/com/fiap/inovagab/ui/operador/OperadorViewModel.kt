package com.fiap.inovagab.ui.operador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fiap.inovagab.core.network.toUserMessage
import com.fiap.inovagab.core.session.AppSession
import com.fiap.inovagab.data.model.Ideia
import com.fiap.inovagab.data.model.Orientacao
import com.fiap.inovagab.data.model.PrioridadeIdeia
import com.fiap.inovagab.data.model.StatusIdeia
import com.fiap.inovagab.data.repository.IdeiaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IdeiaFormUiState(
    val id: String = "",
    val versao: Int = 1,
    val titulo: String = "",
    val area: String = "",
    val descricao: String = "",
    val estrategiaId: String = "",
    val estrategias: List<Orientacao> = emptyList(),
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val erro: String? = null,
    val sucesso: String? = null,
    val concluido: Boolean = false
) {
    val isEdicao: Boolean get() = id.isNotBlank()
}

data class MinhasIdeiasUiState(
    val ideias: List<Ideia> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null,
    val mensagem: String? = null
)

class OperadorViewModel(
    private val repository: IdeiaRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(IdeiaFormUiState())
    val formState: StateFlow<IdeiaFormUiState> = _formState.asStateFlow()

    private val _listState = MutableStateFlow(MinhasIdeiasUiState())
    val listState: StateFlow<MinhasIdeiasUiState> = _listState.asStateFlow()

    fun iniciarFormulario(ideiaId: String?) {
        viewModelScope.launch {
            val estrategias = repository.listarEstrategiasVigentes().getOrDefault(emptyList())
            if (ideiaId.isNullOrBlank()) {
                val defaultId = estrategias.firstOrNull()?.id ?: ""
                _formState.value = IdeiaFormUiState(
                    estrategias = estrategias,
                    estrategiaId = defaultId
                )
                return@launch
            }

            _formState.value = IdeiaFormUiState(carregando = true, estrategias = estrategias)
            repository.buscarPorId(ideiaId).fold(
                onSuccess = { ideia ->
                    if (ideia == null) {
                        _formState.update {
                            it.copy(carregando = false, erro = "Ideia não encontrada.")
                        }
                    } else {
                        _formState.update {
                            it.copy(
                                carregando = false,
                                id = ideia.id,
                                versao = ideia.versao,
                                titulo = ideia.titulo,
                                area = ideia.area,
                                descricao = ideia.descricao,
                                estrategiaId = ideia.estrategiaId,
                                estrategias = estrategias,
                                erro = null
                            )
                        }
                    }
                },
                onFailure = { erro ->
                    _formState.update {
                        it.copy(carregando = false, erro = erro.toUserMessage())
                    }
                }
            )
        }
    }

    fun onTituloChange(value: String) {
        _formState.update { it.copy(titulo = value, erro = null, sucesso = null) }
    }

    fun onAreaChange(value: String) {
        _formState.update { it.copy(area = value, erro = null, sucesso = null) }
    }

    fun onDescricaoChange(value: String) {
        _formState.update { it.copy(descricao = value, erro = null, sucesso = null) }
    }

    fun onEstrategiaChange(id: String) {
        _formState.update { it.copy(estrategiaId = id, erro = null, sucesso = null) }
    }

    fun limparFormulario() {
        iniciarFormulario(null)
    }

    fun consumirNavegacao() {
        _formState.update { it.copy(concluido = false) }
    }

    fun salvarIdeia() {
        val state = _formState.value
        val titulo = state.titulo.trim()
        val area = state.area.trim()
        val descricao = state.descricao.trim()
        val estrategiaId = state.estrategiaId.trim()

        if (titulo.isBlank() || area.isBlank() || descricao.isBlank()) {
            _formState.update { it.copy(erro = "Preencha título, área e descrição.") }
            return
        }
        if (estrategiaId.isBlank()) {
            _formState.update { it.copy(erro = "Selecione a estratégia vigente.") }
            return
        }

        val usuario = AppSession.manager.currentUser.value
        if (usuario == null || usuario.uid.isBlank()) {
            _formState.update { it.copy(erro = "Sessão expirada. Faça login novamente.") }
            return
        }

        _formState.update { it.copy(salvando = true, erro = null, sucesso = null) }

        viewModelScope.launch {
            val ideia = Ideia(
                id = state.id,
                titulo = titulo,
                area = area,
                descricao = descricao,
                autorId = usuario.uid,
                autorNome = usuario.nome,
                status = StatusIdeia.ENVIADA,
                prioridade = PrioridadeIdeia.MEDIA,
                estrategiaId = estrategiaId,
                versao = state.versao
            )

            val resultado = if (state.isEdicao) {
                repository.atualizar(ideia)
            } else {
                repository.criar(ideia).map { }
            }

            resultado.fold(
                onSuccess = {
                    _formState.update {
                        it.copy(
                            salvando = false,
                            sucesso = if (state.isEdicao) {
                                "Ideia atualizada com sucesso!"
                            } else {
                                "Ideia cadastrada com sucesso!"
                            },
                            concluido = true
                        )
                    }
                },
                onFailure = { erro ->
                    _formState.update {
                        it.copy(salvando = false, erro = erro.toUserMessage())
                    }
                }
            )
        }
    }

    fun carregarMinhasIdeias() {
        val usuario = AppSession.manager.currentUser.value
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
                        it.copy(carregando = false, erro = erro.toUserMessage())
                    }
                }
            )
        }
    }

    fun excluirIdeia(ideia: Ideia) {
        viewModelScope.launch {
            repository.excluir(ideia).fold(
                onSuccess = {
                    _listState.update {
                        it.copy(mensagem = "Ideia excluída com sucesso.")
                    }
                    carregarMinhasIdeias()
                },
                onFailure = { erro ->
                    _listState.update { it.copy(erro = erro.toUserMessage()) }
                }
            )
        }
    }

    private fun <T> Result<T>.getOrDefault(default: T): T = getOrElse { default }
}
