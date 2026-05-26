package com.fiap.inovagab.ui.gestor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fiap.inovagab.data.model.Ideia
import com.fiap.inovagab.data.model.PrioridadeIdeia
import com.fiap.inovagab.data.model.StatusIdeia
import com.fiap.inovagab.data.repository.IdeiaRepository
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

class GestorViewModel(
    private val repository: IdeiaRepository = IdeiaRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(GestaoIdeiasUiState())
    val state: StateFlow<GestaoIdeiasUiState> = _state.asStateFlow()

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
}
