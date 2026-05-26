package com.fiap.inovagab.ui.gestor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fiap.inovagab.data.model.Ideia
import com.fiap.inovagab.data.model.PrioridadeIdeia
import com.fiap.inovagab.data.model.Projeto
import com.fiap.inovagab.data.model.StatusIdeia
import com.fiap.inovagab.data.model.StatusProjeto
import com.fiap.inovagab.data.repository.IdeiaRepository
import com.fiap.inovagab.data.repository.ProjetoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GestaoIdeiasUiState(
    val ideias: List<Ideia> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null,
    val mensagem: String? = null
)

data class ProjetosListUiState(
    val projetos: List<Projeto> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null
)

data class ProjetoFormUiState(
    val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val ideiaId: String = "",
    val responsavel: String = "",
    val etapa: String = "",
    val status: StatusProjeto = StatusProjeto.PLANEJADO,
    val investimento: String = "",
    val retornoFinanceiro: String = "",
    val reducaoCustos: String = "",
    val ganhoProdutividade: String = "",
    val prazo: String = "",
    val criadoEm: Long = 0L,
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val erro: String? = null,
    val concluido: Boolean = false
) {
    val isEdicao: Boolean get() = id.isNotBlank()
}

class GestorViewModel(
    private val repository: IdeiaRepository = IdeiaRepository(),
    private val projetoRepository: ProjetoRepository = ProjetoRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(GestaoIdeiasUiState())
    val state: StateFlow<GestaoIdeiasUiState> = _state.asStateFlow()

    private val _projetosListState = MutableStateFlow(ProjetosListUiState())
    val projetosListState: StateFlow<ProjetosListUiState> = _projetosListState.asStateFlow()

    private val _projetoFormState = MutableStateFlow(ProjetoFormUiState())
    val projetoFormState: StateFlow<ProjetoFormUiState> = _projetoFormState.asStateFlow()

    fun carregarIdeias() {
        _state.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            repository.listarTodas().fold(
                onSuccess = { ideias ->
                    _state.update {
                        it.copy(carregando = false, ideias = ideias, erro = null)
                    }
                },
                onFailure = { erro ->
                    _state.update {
                        it.copy(
                            carregando = false,
                            erro = erro.message ?: "Não foi possível carregar as ideias."
                        )
                    }
                }
            )
        }
    }

    fun alterarPrioridade(ideia: Ideia, prioridade: PrioridadeIdeia) {
        if (ideia.id.isBlank() || ideia.prioridade == prioridade) return

        viewModelScope.launch {
            repository.atualizarPrioridade(ideia.id, prioridade).fold(
                onSuccess = {
                    _state.update { atual ->
                        atual.copy(
                            ideias = atual.ideias.map {
                                if (it.id == ideia.id) it.copy(prioridade = prioridade) else it
                            },
                            erro = null
                        )
                    }
                },
                onFailure = { erro ->
                    _state.update {
                        it.copy(erro = erro.message ?: "Não foi possível atualizar a prioridade.")
                    }
                }
            )
        }
    }

    fun alterarStatus(ideia: Ideia, novoStatus: StatusIdeia) {
        if (ideia.id.isBlank() || ideia.status == novoStatus) return

        val jaEstavaAprovada = ideia.status == StatusIdeia.APROVADA

        viewModelScope.launch {
            repository.atualizarStatusComPontuacao(ideia, novoStatus).fold(
                onSuccess = {
                    val mensagemSucesso = if (
                        novoStatus == StatusIdeia.APROVADA && !jaEstavaAprovada
                    ) {
                        "Ideia aprovada! ${IdeiaRepository.PONTOS_POR_APROVACAO} pontos somados ao autor."
                    } else {
                        null
                    }

                    _state.update { atual ->
                        atual.copy(
                            ideias = atual.ideias.map {
                                if (it.id == ideia.id) it.copy(status = novoStatus) else it
                            },
                            erro = null,
                            mensagem = mensagemSucesso
                        )
                    }
                },
                onFailure = { erro ->
                    _state.update {
                        it.copy(erro = erro.message ?: "Não foi possível atualizar o status.")
                    }
                }
            )
        }
    }

    fun consumirMensagem() {
        _state.update { it.copy(mensagem = null) }
    }

    fun carregarProjetos() {
        _projetosListState.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            projetoRepository.listar().fold(
                onSuccess = { projetos ->
                    _projetosListState.update {
                        it.copy(carregando = false, projetos = projetos, erro = null)
                    }
                },
                onFailure = { erro ->
                    _projetosListState.update {
                        it.copy(
                            carregando = false,
                            erro = erro.message ?: "Não foi possível carregar os projetos."
                        )
                    }
                }
            )
        }
    }

    fun iniciarFormularioProjeto(id: String?) {
        if (id.isNullOrBlank()) {
            _projetoFormState.value = ProjetoFormUiState()
            return
        }

        _projetoFormState.value = ProjetoFormUiState(id = id, carregando = true)
        viewModelScope.launch {
            projetoRepository.buscarPorId(id).fold(
                onSuccess = { projeto ->
                    if (projeto == null) {
                        _projetoFormState.update {
                            it.copy(
                                carregando = false,
                                erro = "Projeto não encontrado."
                            )
                        }
                    } else {
                        _projetoFormState.update {
                            it.copy(
                                carregando = false,
                                id = projeto.id,
                                nome = projeto.nome,
                                descricao = projeto.descricao,
                                ideiaId = projeto.ideiaId,
                                responsavel = projeto.responsavel,
                                etapa = projeto.etapa,
                                status = projeto.status,
                                investimento = formatarValorEdicao(projeto.investimento),
                                retornoFinanceiro = formatarValorEdicao(projeto.retornoFinanceiro),
                                reducaoCustos = formatarValorEdicao(projeto.reducaoCustos),
                                ganhoProdutividade = formatarValorEdicao(projeto.ganhoProdutividade),
                                prazo = projeto.prazo,
                                criadoEm = projeto.criadoEm,
                                erro = null
                            )
                        }
                    }
                },
                onFailure = { erro ->
                    _projetoFormState.update {
                        it.copy(
                            carregando = false,
                            erro = erro.message ?: "Não foi possível abrir o projeto."
                        )
                    }
                }
            )
        }
    }

    fun onProjetoNomeChange(value: String) {
        _projetoFormState.update { it.copy(nome = value, erro = null) }
    }

    fun onProjetoDescricaoChange(value: String) {
        _projetoFormState.update { it.copy(descricao = value, erro = null) }
    }

    fun onProjetoResponsavelChange(value: String) {
        _projetoFormState.update { it.copy(responsavel = value, erro = null) }
    }

    fun onProjetoEtapaChange(value: String) {
        _projetoFormState.update { it.copy(etapa = value, erro = null) }
    }

    fun onProjetoStatusChange(value: StatusProjeto) {
        _projetoFormState.update { it.copy(status = value, erro = null) }
    }

    fun onProjetoInvestimentoChange(value: String) {
        _projetoFormState.update { it.copy(investimento = sanitizarNumero(value), erro = null) }
    }

    fun onProjetoRetornoFinanceiroChange(value: String) {
        _projetoFormState.update {
            it.copy(retornoFinanceiro = sanitizarNumero(value), erro = null)
        }
    }

    fun onProjetoReducaoCustosChange(value: String) {
        _projetoFormState.update { it.copy(reducaoCustos = sanitizarNumero(value), erro = null) }
    }

    fun onProjetoGanhoProdutividadeChange(value: String) {
        _projetoFormState.update {
            it.copy(ganhoProdutividade = sanitizarNumero(value), erro = null)
        }
    }

    fun onProjetoPrazoChange(value: String) {
        _projetoFormState.update { it.copy(prazo = value, erro = null) }
    }

    fun salvarProjeto() {
        val atual = _projetoFormState.value
        val nome = atual.nome.trim()
        val descricao = atual.descricao.trim()
        val responsavel = atual.responsavel.trim()
        val etapa = atual.etapa.trim()
        val prazo = atual.prazo.trim()

        if (nome.isBlank() || descricao.isBlank() || responsavel.isBlank() ||
            etapa.isBlank() || prazo.isBlank()
        ) {
            _projetoFormState.update {
                it.copy(
                    erro = "Preencha nome, descrição, responsável, etapa e prazo."
                )
            }
            return
        }

        val investimento = parseNumeroOuNulo(atual.investimento)
        val retornoFinanceiro = parseNumeroOuNulo(atual.retornoFinanceiro)
        val reducaoCustos = parseNumeroOuNulo(atual.reducaoCustos)
        val ganhoProdutividade = parseNumeroOuNulo(atual.ganhoProdutividade)

        if (investimento == null || retornoFinanceiro == null ||
            reducaoCustos == null || ganhoProdutividade == null
        ) {
            _projetoFormState.update {
                it.copy(
                    erro = "Informe valores numéricos válidos (use ponto ou vírgula)."
                )
            }
            return
        }

        _projetoFormState.update { it.copy(salvando = true, erro = null) }

        viewModelScope.launch {
            val resultado = if (atual.isEdicao) {
                projetoRepository.atualizar(
                    Projeto(
                        id = atual.id,
                        nome = nome,
                        descricao = descricao,
                        ideiaId = atual.ideiaId,
                        responsavel = responsavel,
                        etapa = etapa,
                        status = atual.status,
                        investimento = investimento,
                        retornoFinanceiro = retornoFinanceiro,
                        reducaoCustos = reducaoCustos,
                        ganhoProdutividade = ganhoProdutividade,
                        prazo = prazo,
                        criadoEm = if (atual.criadoEm > 0L) atual.criadoEm else System.currentTimeMillis()
                    )
                )
            } else {
                projetoRepository.criar(
                    Projeto(
                        nome = nome,
                        descricao = descricao,
                        ideiaId = atual.ideiaId,
                        responsavel = responsavel,
                        etapa = etapa,
                        status = atual.status,
                        investimento = investimento,
                        retornoFinanceiro = retornoFinanceiro,
                        reducaoCustos = reducaoCustos,
                        ganhoProdutividade = ganhoProdutividade,
                        prazo = prazo
                    )
                ).map { }
            }

            resultado.fold(
                onSuccess = {
                    _projetoFormState.update {
                        it.copy(salvando = false, concluido = true, erro = null)
                    }
                    carregarProjetos()
                },
                onFailure = { erro ->
                    _projetoFormState.update {
                        it.copy(
                            salvando = false,
                            erro = erro.message ?: "Não foi possível salvar o projeto."
                        )
                    }
                }
            )
        }
    }

    fun consumirNavegacaoProjeto() {
        _projetoFormState.update { it.copy(concluido = false) }
    }

    fun limparFormularioProjeto() {
        _projetoFormState.value = ProjetoFormUiState()
    }

    private fun sanitizarNumero(valor: String): String {
        return valor.filter { it.isDigit() || it == '.' || it == ',' }
    }

    private fun parseNumeroOuNulo(valor: String): Double? {
        val limpo = valor.trim()
        if (limpo.isBlank()) return 0.0

        val temVirgula = limpo.contains(',')
        val temPonto = limpo.contains('.')
        val normalizado = when {
            temVirgula && temPonto -> limpo.replace(".", "").replace(',', '.')
            temVirgula -> limpo.replace(',', '.')
            else -> limpo
        }
        return normalizado.toDoubleOrNull()
    }

    private fun formatarValorEdicao(valor: Double): String {
        if (valor == 0.0) return ""
        return if (valor % 1.0 == 0.0) {
            valor.toLong().toString()
        } else {
            valor.toString()
        }
    }
}
