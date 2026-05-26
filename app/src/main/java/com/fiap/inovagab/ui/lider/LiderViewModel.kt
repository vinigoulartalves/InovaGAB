package com.fiap.inovagab.ui.lider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fiap.inovagab.data.model.Orientacao
import com.fiap.inovagab.data.model.Projeto
import com.fiap.inovagab.data.repository.OrientacaoRepository
import com.fiap.inovagab.data.repository.ProjetoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrientacoesListUiState(
    val orientacoes: List<Orientacao> = emptyList(),
    val loading: Boolean = false,
    val erro: String? = null
)

data class ProjetosConsultaUiState(
    val projetos: List<Projeto> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null
)

data class OrientacaoFormUiState(
    val id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val excluindo: Boolean = false,
    val erro: String? = null,
    val concluido: Boolean = false
) {
    val isEdicao: Boolean get() = id.isNotBlank()
}

class LiderViewModel(
    private val repository: OrientacaoRepository = OrientacaoRepository(),
    private val projetoRepository: ProjetoRepository = ProjetoRepository()
) : ViewModel() {

    private val _listState = MutableStateFlow(OrientacoesListUiState())
    val listState: StateFlow<OrientacoesListUiState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(OrientacaoFormUiState())
    val formState: StateFlow<OrientacaoFormUiState> = _formState.asStateFlow()

    private val _projetosState = MutableStateFlow(ProjetosConsultaUiState())
    val projetosState: StateFlow<ProjetosConsultaUiState> = _projetosState.asStateFlow()

    fun consultarProjetos() {
        _projetosState.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            projetoRepository.listar().fold(
                onSuccess = { projetos ->
                    _projetosState.update {
                        it.copy(carregando = false, projetos = projetos, erro = null)
                    }
                },
                onFailure = { erro ->
                    _projetosState.update {
                        it.copy(
                            carregando = false,
                            erro = erro.message ?: "Não foi possível consultar os projetos."
                        )
                    }
                }
            )
        }
    }

    fun carregarOrientacoes() {
        _listState.update { it.copy(loading = true, erro = null) }
        viewModelScope.launch {
            repository.listar().fold(
                onSuccess = { lista ->
                    _listState.update {
                        it.copy(loading = false, orientacoes = lista, erro = null)
                    }
                },
                onFailure = { erro ->
                    _listState.update {
                        it.copy(
                            loading = false,
                            erro = erro.message ?: "Não foi possível carregar as orientações."
                        )
                    }
                }
            )
        }
    }

    fun iniciarFormulario(id: String?) {
        if (id.isNullOrBlank()) {
            _formState.value = OrientacaoFormUiState()
            return
        }

        _formState.value = OrientacaoFormUiState(id = id, carregando = true)
        viewModelScope.launch {
            repository.buscarPorId(id).fold(
                onSuccess = { orientacao ->
                    if (orientacao == null) {
                        _formState.update {
                            it.copy(
                                carregando = false,
                                erro = "Orientação não encontrada."
                            )
                        }
                    } else {
                        _formState.update {
                            it.copy(
                                carregando = false,
                                id = orientacao.id,
                                titulo = orientacao.titulo,
                                descricao = orientacao.descricao,
                                erro = null
                            )
                        }
                    }
                },
                onFailure = { erro ->
                    _formState.update {
                        it.copy(
                            carregando = false,
                            erro = erro.message ?: "Não foi possível abrir a orientação."
                        )
                    }
                }
            )
        }
    }

    fun onTituloChange(value: String) {
        _formState.update { it.copy(titulo = value, erro = null) }
    }

    fun onDescricaoChange(value: String) {
        _formState.update { it.copy(descricao = value, erro = null) }
    }

    fun salvar() {
        val state = _formState.value
        val titulo = state.titulo.trim()
        val descricao = state.descricao.trim()

        if (titulo.isBlank() || descricao.isBlank()) {
            _formState.update { it.copy(erro = "Informe o título e a descrição.") }
            return
        }

        _formState.update { it.copy(salvando = true, erro = null) }

        viewModelScope.launch {
            val resultado = if (state.isEdicao) {
                repository.atualizar(
                    Orientacao(
                        id = state.id,
                        titulo = titulo,
                        descricao = descricao
                    )
                )
            } else {
                repository.criar(
                    Orientacao(
                        titulo = titulo,
                        descricao = descricao
                    )
                ).map { }
            }

            resultado.fold(
                onSuccess = {
                    _formState.update { it.copy(salvando = false, concluido = true, erro = null) }
                },
                onFailure = { erro ->
                    _formState.update {
                        it.copy(
                            salvando = false,
                            erro = erro.message ?: "Não foi possível salvar a orientação."
                        )
                    }
                }
            )
        }
    }

    fun excluir(id: String) {
        if (id.isBlank()) return
        _formState.update { it.copy(excluindo = true, erro = null) }
        viewModelScope.launch {
            repository.excluir(id).fold(
                onSuccess = {
                    _formState.update { it.copy(excluindo = false, concluido = true, erro = null) }
                    carregarOrientacoes()
                },
                onFailure = { erro ->
                    _formState.update {
                        it.copy(
                            excluindo = false,
                            erro = erro.message ?: "Não foi possível excluir a orientação."
                        )
                    }
                }
            )
        }
    }

    fun excluirDaLista(id: String) {
        excluir(id)
    }

    fun consumirNavegacao() {
        _formState.update { it.copy(concluido = false) }
    }

    fun limparFormulario() {
        _formState.value = OrientacaoFormUiState()
    }
}
