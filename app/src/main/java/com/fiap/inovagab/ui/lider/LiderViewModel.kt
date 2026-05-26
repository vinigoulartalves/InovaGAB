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

data class DashboardUiState(
    val carregando: Boolean = false,
    val erro: String? = null,
    val totalProjetos: Int = 0,
    val investimentoTotal: Double = 0.0,
    val retornoTotal: Double = 0.0,
    val lucroObtido: Double = 0.0,
    val roiGeral: Double = 0.0,
    val reducaoCustosTotal: Double = 0.0,
    val ganhoProdutividadeMedio: Double = 0.0
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

    private val _dashboardState = MutableStateFlow(DashboardUiState())
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    fun carregarDashboard() {
        _dashboardState.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            projetoRepository.listar().fold(
                onSuccess = { projetos ->
                    _dashboardState.value = calcularDashboard(projetos)
                },
                onFailure = { erro ->
                    _dashboardState.update {
                        it.copy(
                            carregando = false,
                            erro = erro.message ?: "Não foi possível carregar a dashboard."
                        )
                    }
                }
            )
        }
    }

    private fun calcularDashboard(projetos: List<Projeto>): DashboardUiState {
        val totalProjetos = projetos.size
        val investimentoTotal = projetos.sumOf { it.investimento }
        val retornoTotal = projetos.sumOf { it.retornoFinanceiro }
        val lucroObtido = retornoTotal - investimentoTotal
        val roiGeral = if (investimentoTotal == 0.0) {
            0.0
        } else {
            ((retornoTotal - investimentoTotal) / investimentoTotal) * 100
        }
        val reducaoCustosTotal = projetos.sumOf { it.reducaoCustos }
        val ganhoProdutividadeMedio = if (projetos.isEmpty()) {
            0.0
        } else {
            projetos.sumOf { it.ganhoProdutividade } / projetos.size
        }

        return DashboardUiState(
            carregando = false,
            erro = null,
            totalProjetos = totalProjetos,
            investimentoTotal = investimentoTotal,
            retornoTotal = retornoTotal,
            lucroObtido = lucroObtido,
            roiGeral = roiGeral,
            reducaoCustosTotal = reducaoCustosTotal,
            ganhoProdutividadeMedio = ganhoProdutividadeMedio
        )
    }

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

    fun consumirNavegacao() {
        _formState.update { it.copy(concluido = false) }
    }
}
