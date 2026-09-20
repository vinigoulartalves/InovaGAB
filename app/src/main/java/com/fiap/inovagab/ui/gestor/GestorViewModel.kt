package com.fiap.inovagab.ui.gestor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fiap.inovagab.core.network.toUserMessage
import com.fiap.inovagab.core.session.AppSession
import com.fiap.inovagab.data.model.AnaliseIa
import com.fiap.inovagab.data.model.Ideia
import com.fiap.inovagab.data.model.Orientacao
import com.fiap.inovagab.data.model.PrioridadeIdeia
import com.fiap.inovagab.data.model.Projeto
import com.fiap.inovagab.data.model.StatusIdeia
import com.fiap.inovagab.data.model.StatusProjeto
import com.fiap.inovagab.data.remote.dto.ResponsavelResumoDto
import com.fiap.inovagab.data.repository.IaRepository
import com.fiap.inovagab.data.repository.IdeiaRepository
import com.fiap.inovagab.data.repository.ProjetoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class IaPainelUiState(
    val carregando: Boolean = false,
    val erro: String? = null,
    val analise: AnaliseIa? = null
)

data class GestaoIdeiasUiState(
    val ideias: List<Ideia> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null,
    val mensagem: String? = null,
    val iaPorIdeia: Map<String, IaPainelUiState> = emptyMap()
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
    val ideiaVersao: Int = 1,
    val estrategiaId: String = "",
    val estrategiaTitulo: String = "",
    val bloquearEstrategia: Boolean = false,
    val modoConversao: Boolean = false,
    val estrategias: List<Orientacao> = emptyList(),
    val responsaveis: List<ResponsavelResumoDto> = emptyList(),
    val responsavel: String = "",
    val responsavelId: String = "",
    val etapa: String = "",
    val status: StatusProjeto = StatusProjeto.PLANEJADO,
    val versao: Int = 1,
    val investimento: String = "",
    val retornoFinanceiro: String = "",
    val reducaoCustos: String = "",
    val ganhoProdutividade: String = "",
    val prazo: String = "",
    val criadoEm: Long = 0L,
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val excluindo: Boolean = false,
    val erro: String? = null,
    val concluido: Boolean = false
) {
    val isEdicao: Boolean get() = id.isNotBlank() && !modoConversao
}

class GestorViewModel(
    private val repository: IdeiaRepository,
    private val projetoRepository: ProjetoRepository,
    private val iaRepository: IaRepository
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
                        it.copy(carregando = false, erro = erro.toUserMessage())
                    }
                }
            )
        }
    }

    fun alterarPrioridade(ideia: Ideia, prioridade: PrioridadeIdeia) {
        if (ideia.id.isBlank() || ideia.prioridade == prioridade) return
        viewModelScope.launch {
            repository.atualizarPrioridade(ideia, prioridade).fold(
                onSuccess = { carregarIdeias() },
                onFailure = { erro ->
                    _state.update { it.copy(erro = erro.toUserMessage()) }
                }
            )
        }
    }

    fun alterarStatus(ideia: Ideia, novoStatus: StatusIdeia) {
        if (ideia.id.isBlank() || ideia.status == novoStatus) return
        viewModelScope.launch {
            repository.atualizarStatusComPontuacao(ideia, novoStatus).fold(
                onSuccess = {
                    val mensagemSucesso = if (novoStatus == StatusIdeia.APROVADA) {
                        "Ideia aprovada com sucesso."
                    } else {
                        null
                    }
                    carregarIdeias()
                    _state.update { it.copy(mensagem = mensagemSucesso, erro = null) }
                },
                onFailure = { erro ->
                    _state.update { it.copy(erro = erro.toUserMessage()) }
                }
            )
        }
    }

    fun analisarComIa(ideia: Ideia) {
        val id = ideia.id
        _state.update {
            val atual = it.iaPorIdeia[id] ?: IaPainelUiState()
            it.copy(
                iaPorIdeia = it.iaPorIdeia + (id to atual.copy(carregando = true, erro = null))
            )
        }
        viewModelScope.launch {
            iaRepository.solicitarAnalise(id).fold(
                onSuccess = { analise ->
                    _state.update {
                        it.copy(
                            iaPorIdeia = it.iaPorIdeia + (
                                id to IaPainelUiState(
                                    carregando = false,
                                    analise = analise,
                                    erro = null
                                )
                                )
                        )
                    }
                },
                onFailure = { erro ->
                    _state.update {
                        it.copy(
                            iaPorIdeia = it.iaPorIdeia + (
                                id to IaPainelUiState(
                                    carregando = false,
                                    erro = erro.toUserMessage()
                                )
                                )
                        )
                    }
                }
            )
        }
    }

    fun aplicarPrioridadeSugerida(ideia: Ideia, analise: AnaliseIa) {
        val justificativa =
            "Prioridade aplicada conforme análise IA (${analise.pontuacaoTotal} pts): ${analise.justificativa}"
        viewModelScope.launch {
            repository.aplicarPrioridadeSugerida(
                ideia,
                analise.prioridadeSugerida,
                justificativa.take(2000)
            ).fold(
                onSuccess = {
                    _state.update { it.copy(mensagem = "Prioridade sugerida aplicada via avaliação.") }
                    carregarIdeias()
                },
                onFailure = { erro ->
                    _state.update { it.copy(erro = erro.toUserMessage()) }
                }
            )
        }
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
                        it.copy(carregando = false, erro = erro.toUserMessage())
                    }
                }
            )
        }
    }

    fun iniciarFormularioProjeto(projetoId: String?, ideiaConversaoId: String? = null) {
        viewModelScope.launch {
            val estrategias = projetoRepository.listarEstrategiasVigentes().getOrDefault(emptyList())
            val responsaveis = projetoRepository.listarResponsaveis().getOrDefault(emptyList())

            if (!ideiaConversaoId.isNullOrBlank()) {
                repository.buscarPorId(ideiaConversaoId).fold(
                    onSuccess = { ideia ->
                        if (ideia == null) {
                            _projetoFormState.value = ProjetoFormUiState(
                                erro = "Ideia não encontrada para conversão.",
                                estrategias = estrategias,
                                responsaveis = responsaveis
                            )
                        } else {
                            val estrategiaTitulo = estrategias
                                .find { it.id == ideia.estrategiaId }?.titulo ?: ideia.estrategiaId
                            val resp = responsaveis.firstOrNull()
                            _projetoFormState.value = ProjetoFormUiState(
                                ideiaId = ideia.id,
                                ideiaVersao = ideia.versao,
                                nome = ideia.titulo,
                                descricao = ideia.descricao,
                                estrategiaId = ideia.estrategiaId,
                                estrategiaTitulo = estrategiaTitulo,
                                bloquearEstrategia = true,
                                modoConversao = true,
                                estrategias = estrategias,
                                responsaveis = responsaveis,
                                responsavelId = resp?.id ?: "",
                                responsavel = resp?.nome ?: "",
                                prazo = LocalDate.now().plusMonths(6).toString()
                            )
                        }
                    },
                    onFailure = { erro ->
                        _projetoFormState.value = ProjetoFormUiState(
                            erro = erro.toUserMessage(),
                            estrategias = estrategias,
                            responsaveis = responsaveis
                        )
                    }
                )
                return@launch
            }

            if (projetoId.isNullOrBlank()) {
                val defaultEstrategia = estrategias.firstOrNull()
                val resp = responsaveis.firstOrNull()
                _projetoFormState.value = ProjetoFormUiState(
                    estrategias = estrategias,
                    responsaveis = responsaveis,
                    estrategiaId = defaultEstrategia?.id ?: "",
                    estrategiaTitulo = defaultEstrategia?.titulo ?: "",
                    responsavelId = resp?.id ?: "",
                    responsavel = resp?.nome ?: "",
                    prazo = LocalDate.now().plusMonths(6).toString()
                )
                return@launch
            }

            _projetoFormState.value = ProjetoFormUiState(
                id = projetoId,
                carregando = true,
                estrategias = estrategias,
                responsaveis = responsaveis
            )
            projetoRepository.buscarPorId(projetoId).fold(
                onSuccess = { projeto ->
                    if (projeto == null) {
                        _projetoFormState.update {
                            it.copy(carregando = false, erro = "Projeto não encontrado.")
                        }
                    } else {
                        val tituloEstrategia = estrategias.find { it.id == projeto.estrategiaId }?.titulo
                            ?: projeto.estrategiaId
                        _projetoFormState.update {
                            it.copy(
                                carregando = false,
                                id = projeto.id,
                                nome = projeto.nome,
                                descricao = projeto.descricao,
                                ideiaId = projeto.ideiaId,
                                responsavel = projeto.responsavel,
                                responsavelId = projeto.responsavelId,
                                estrategiaId = projeto.estrategiaId,
                                estrategiaTitulo = tituloEstrategia,
                                versao = projeto.versao,
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
                        it.copy(carregando = false, erro = erro.toUserMessage())
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

    fun onProjetoResponsavelIdChange(id: String, nome: String) {
        _projetoFormState.update { it.copy(responsavelId = id, responsavel = nome, erro = null) }
    }

    fun onProjetoEstrategiaChange(id: String, titulo: String) {
        if (_projetoFormState.value.bloquearEstrategia) return
        _projetoFormState.update {
            it.copy(estrategiaId = id, estrategiaTitulo = titulo, erro = null)
        }
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
        val etapa = atual.etapa.trim()
        val prazo = atual.prazo.trim()
        val responsavelId = atual.responsavelId.trim()

        if (nome.isBlank() || descricao.isBlank() || etapa.isBlank() || prazo.isBlank()) {
            _projetoFormState.update {
                it.copy(erro = "Preencha nome, descrição, etapa e prazo.")
            }
            return
        }
        if (responsavelId.isBlank()) {
            _projetoFormState.update { it.copy(erro = "Selecione o responsável.") }
            return
        }
        if (atual.estrategiaId.isBlank()) {
            _projetoFormState.update { it.copy(erro = "Selecione a estratégia.") }
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
                it.copy(erro = "Informe valores numéricos válidos (use ponto ou vírgula).")
            }
            return
        }

        if (!prazoValido(prazo)) {
            _projetoFormState.update {
                it.copy(erro = "Informe o prazo no formato AAAA-MM-DD.")
            }
            return
        }

        _projetoFormState.update { it.copy(salvando = true, erro = null) }

        val projeto = Projeto(
            id = atual.id,
            nome = nome,
            descricao = descricao,
            ideiaId = atual.ideiaId,
            estrategiaId = atual.estrategiaId,
            responsavel = atual.responsavel,
            responsavelId = responsavelId,
            etapa = etapa,
            status = atual.status,
            versao = atual.versao,
            investimento = investimento,
            retornoFinanceiro = retornoFinanceiro,
            reducaoCustos = reducaoCustos,
            ganhoProdutividade = ganhoProdutividade,
            prazo = prazo,
            criadoEm = if (atual.criadoEm > 0L) atual.criadoEm else System.currentTimeMillis()
        )

        viewModelScope.launch {
            val resultado = when {
                atual.modoConversao -> {
                    val ideia = repository.buscarPorId(atual.ideiaId).getOrNull()
                    if (ideia == null) {
                        Result.failure(IllegalStateException("Ideia não encontrada para conversão."))
                    } else {
                        repository.converterEmProjeto(ideia, projeto).map { }
                    }
                }
                atual.isEdicao -> projetoRepository.atualizar(projeto)
                else -> projetoRepository.criar(projeto).map { }
            }

            resultado.fold(
                onSuccess = {
                    _projetoFormState.update {
                        it.copy(salvando = false, concluido = true, erro = null)
                    }
                    carregarProjetos()
                    carregarIdeias()
                },
                onFailure = { erro ->
                    _projetoFormState.update {
                        it.copy(salvando = false, erro = erro.toUserMessage())
                    }
                }
            )
        }
    }

    fun excluirProjeto() {
        val atual = _projetoFormState.value
        if (atual.id.isBlank()) return
        _projetoFormState.update { it.copy(excluindo = true, erro = null) }
        viewModelScope.launch {
            projetoRepository.excluir(
                Projeto(
                    id = atual.id,
                    versao = atual.versao,
                    nome = atual.nome,
                    estrategiaId = atual.estrategiaId,
                    responsavelId = atual.responsavelId,
                    prazo = atual.prazo
                )
            ).fold(
                onSuccess = {
                    _projetoFormState.update {
                        it.copy(excluindo = false, concluido = true)
                    }
                    carregarProjetos()
                },
                onFailure = { erro ->
                    _projetoFormState.update {
                        it.copy(excluindo = false, erro = erro.toUserMessage())
                    }
                }
            )
        }
    }

    fun consumirNavegacaoProjeto() {
        _projetoFormState.update { it.copy(concluido = false) }
    }

    private fun prazoValido(prazo: String): Boolean =
        runCatching { LocalDate.parse(prazo) }.isSuccess

    private fun sanitizarNumero(valor: String): String =
        valor.filter { it.isDigit() || it == '.' || it == ',' }

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
        return if (valor % 1.0 == 0.0) valor.toLong().toString() else valor.toString()
    }

    private fun <T> Result<T>.getOrDefault(default: T): T = getOrElse { default }
}
