package com.fiap.inovagab.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fiap.inovagab.InovaGabApp
import com.fiap.inovagab.ui.gestor.GestorViewModel
import com.fiap.inovagab.ui.lider.LiderViewModel
import com.fiap.inovagab.ui.login.LoginViewModel
import com.fiap.inovagab.ui.operador.OperadorViewModel
import com.fiap.inovagab.ui.shared.RankingViewModel

class AppViewModelFactory(
    private val app: InovaGabApp
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(LoginViewModel::class.java) ->
            LoginViewModel(app.authRepository) as T
        modelClass.isAssignableFrom(OperadorViewModel::class.java) ->
            OperadorViewModel(app.ideiaRepository) as T
        modelClass.isAssignableFrom(GestorViewModel::class.java) ->
            GestorViewModel(app.ideiaRepository, app.projetoRepository, app.iaRepository) as T
        modelClass.isAssignableFrom(LiderViewModel::class.java) ->
            LiderViewModel(
                app.orientacaoRepository,
                app.projetoRepository,
                app.relatorioRepository
            ) as T
        modelClass.isAssignableFrom(RankingViewModel::class.java) ->
            RankingViewModel(app.userRepository) as T
        else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    }
}
