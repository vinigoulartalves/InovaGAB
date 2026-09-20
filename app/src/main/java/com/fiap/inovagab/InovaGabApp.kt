package com.fiap.inovagab

import android.app.Application
import com.fiap.inovagab.core.network.NetworkModule
import com.fiap.inovagab.core.session.SessionManager
import com.fiap.inovagab.core.session.TokenStore
import com.fiap.inovagab.data.repository.AuthRepository
import com.fiap.inovagab.data.repository.IdeiaRepository
import com.fiap.inovagab.data.repository.OrientacaoRepository
import com.fiap.inovagab.data.repository.ProjetoRepository
import com.fiap.inovagab.data.repository.RelatorioRepository
import com.fiap.inovagab.data.repository.UserRepository

class InovaGabApp : Application() {

    lateinit var tokenStore: TokenStore
        private set

    lateinit var network: NetworkModule
        private set

    lateinit var sessionManager: SessionManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var ideiaRepository: IdeiaRepository
        private set

    lateinit var projetoRepository: ProjetoRepository
        private set

    lateinit var orientacaoRepository: OrientacaoRepository
        private set

    lateinit var userRepository: UserRepository
        private set

    lateinit var relatorioRepository: RelatorioRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        tokenStore = TokenStore(this)

        lateinit var sessionRef: SessionManager
        network = NetworkModule(
            tokenStore = tokenStore,
            onSessionExpired = { sessionRef.notifySessionExpired() }
        )
        sessionRef = SessionManager(tokenStore, network).also { sessionManager = it }

        authRepository = AuthRepository(sessionManager, network.authApi, tokenStore)
        ideiaRepository = IdeiaRepository(network.ideiasApi, network.estrategiasApi)
        projetoRepository = ProjetoRepository(
            network.projetosApi,
            network.usuariosApi,
            network.estrategiasApi,
            sessionManager
        )
        orientacaoRepository = OrientacaoRepository(network.estrategiasApi)
        userRepository = UserRepository(network.rankingApi)
        relatorioRepository = RelatorioRepository(network.relatoriosApi, network.projetosApi)
    }

    companion object {
        lateinit var instance: InovaGabApp
            private set
    }
}
