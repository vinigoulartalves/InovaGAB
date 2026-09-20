package com.fiap.inovagab.ui.lider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fiap.inovagab.core.network.toUserMessage
import com.fiap.inovagab.data.model.DashboardFiltros
import com.fiap.inovagab.data.model.DashboardReport
import com.fiap.inovagab.data.model.EstrategiaHistorico
import com.fiap.inovagab.data.model.Orientacao
import com.fiap.inovagab.data.model.Projeto
import com.fiap.inovagab.data.repository.OrientacaoRepository
import com.fiap.inovagab.data.repository.ProjetoRepository
import com.fiap.inovagab.data.repository.RelatorioRepository
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
    val vazio: Boolean = false,
    val report: DashboardReport? = null,
    val filtros: DashboardFiltros = DashboardFiltros(),
    val estrategiasOpcoes: List<Orientacao> = emptyList(),
    val projetosOpcoes: List<Projeto> = emptyList()
)

data class OrientacaoFormUiState(
    val id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val categoria: String = "",
    val campanha: String = "",
    val inicioVigencia: String = "",
    val fimVigencia: String = "",
    val ativa: Boolean = true,
    val versao: Int = 1,
    val historico: List<EstrategiaHistorico> = emptyList(),
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val excluindo: Boolean = false,
    val erro: String? = null,
    val concluido: Boolean = false
) {
    val isEdicao: Boolean get() = id.isNotBlank()
}

class LiderViewModel(
    private val repository: OrientacaoRepository,
    private val projetoRepository: ProjetoRepository,
    private val relatorioRepository: RelatorioRepository
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
        val filtros = _dashboardState.value.filtros
        _dashboardState.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            relatorioRepository.carregarDashboard(filtros).fold(
                onSuccess = { report ->
                    val vazio = report.totalProjetos == 0 &&
                        report.investimentoRetornoPorEstrategia.isEmpty()
                    _dashboardState.update {
                        it.copy(
                            carregando = false,
                            report = report,
                            vazio = vazio,
                            erro = null
                        )
                    }
                },
                onFailure = { erro ->
                    _dashboardState.update {
                        it.copy(
                            carregando = false,
                            erro = erro.toUserMessage()
                        )
                    }
                }
            )
        }
    }

    fun carregarOpcoesDashboard() {
        viewModelScope.launch {
            val estrategias = repository.listar().getOrDefault(emptyList())
            val projetos = projetoRepository.listar().getOrDefault(emptyList())
            _dashboardState.update {
                it.copy(estrategiasOpcoes = estrategias, projetosOpcoes = projetos)
            }
        }
    }

    fun onFiltroEstrategiaChange(id: String?) {
        _dashboardState.update { it.copy(filtros = it.filtros.copy(estrategiaId = id)) }
    }

    fun onFiltroProjetoChange(id: String?) {
        _dashboardState.update { it.copy(filtros = it.filtros.copy(projetoId = id)) }
    }

    fun onFiltroInicioChange(value: String) {
        _dashboardState.update { it.copy(filtros = it.filtros.copy(inicio = value.ifBlank { null })) }
    }

    fun onFiltroFimChange(value: String) {
        _dashboardState.update { it.copy(filtros = it.filtros.copy(fim = value.ifBlank { null })) }
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
                        it.copy(carregando = false, erro = erro.toUserMessage())
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
                        it.copy(loading = false, erro = erro.toUserMessage())
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
                            it.copy(carregando = false, erro = "Orientação não encontrada.")
                        }
                    } else {
                        val hist = repository.historico(id).getOrDefault(emptyList())
                        _formState.update {
                            it.copy(
                                carregando = false,
                                id = orientacao.id,
                                titulo = orientacao.titulo,
                                descricao = orientacao.descricao,
                                categoria = orientacao.categoria,
                                campanha = orientacao.campanha,
                                inicioVigencia = orientacao.inicioVigencia,
                                fimVigencia = orientacao.fimVigencia.orEmpty(),
                                ativa = orientacao.ativa,
                                versao = orientacao.versao,
                                historico = hist,
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
        _formState.update { it.copy(titulo = value, erro = null) }
    }

    fun onDescricaoChange(value: String) {
        _formState.update { it.copy(descricao = value, erro = null) }
    }

    fun onCategoriaChange(value: String) {
        _formState.update { it.copy(categoria = value, erro = null) }
    }

    fun onCampanhaChange(value: String) {
        _formState.update { it.copy(campanha = value, erro = null) }
    }

    fun onInicioVigenciaChange(value: String) {
        _formState.update { it.copy(inicioVigencia = value, erro = null) }
    }

    fun onFimVigenciaChange(value: String) {
        _formState.update { it.copy(fimVigencia = value, erro = null) }
    }

    fun onAtivaChange(value: Boolean) {
        _formState.update { it.copy(ativa = value, erro = null) }
    }

    fun salvar() {
        val state = _formState.value
        val titulo = state.titulo.trim()
        val descricao = state.descricao.trim()
        val categoria = state.categoria.trim()
        val campanha = state.campanha.trim()
        val inicio = state.inicioVigencia.trim()

        if (titulo.isBlank() || descricao.isBlank() || categoria.isBlank() ||
            campanha.isBlank() || inicio.isBlank()
        ) {
            _formState.update {
                it.copy(erro = "Preencha título, descrição, categoria, campanha e início da vigência.")
            }
            return
        }

        _formState.update { it.copy(salvando = true, erro = null) }

        val orientacao = Orientacao(
            id = state.id,
            titulo = titulo,
            descricao = descricao,
            categoria = categoria,
            campanha = campanha,
            inicioVigencia = inicio,
            fimVigencia = state.fimVigencia.ifBlank { null },
            ativa = state.ativa,
            versao = state.versao
        )

        viewModelScope.launch {
            val resultado = if (state.isEdicao) {
                repository.atualizar(orientacao)
            } else {
                repository.criar(orientacao).map { }
            }

            resultado.fold(
                onSuccess = {
                    _formState.update { it.copy(salvando = false, concluido = true, erro = null) }
                },
                onFailure = { erro ->
                    _formState.update {
                        it.copy(salvando = false, erro = erro.toUserMessage())
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
                        it.copy(excluindo = false, erro = erro.toUserMessage())
                    }
                }
            )
        }
    }

    fun consumirNavegacao() {
        _formState.update { it.copy(concluido = false) }
    }

    private fun <T> Result<T>.getOrDefault(default: T): T =
        getOrElse { default }
}
