package com.fiap.inovagab.core.session

import com.fiap.inovagab.core.network.ApiCallRunner
import com.fiap.inovagab.core.network.NetworkModule
import com.fiap.inovagab.data.model.User
import com.fiap.inovagab.data.remote.toUser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

class SessionManager(
    private val tokenStore: TokenStore,
    private val network: NetworkModule
) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    fun setUser(user: User?) {
        _currentUser.value = user
    }

    fun clear() {
        _currentUser.value = null
        tokenStore.clear()
    }

    fun notifySessionExpired() {
        clear()
        _sessionExpired.tryEmit(Unit)
    }

    val isLogged: Boolean
        get() = _currentUser.value != null && tokenStore.getAccessToken() != null

    suspend fun restoreSession(): Boolean {
        if (!tokenStore.hasRefreshToken()) return false
        return ApiCallRunner.run { network.authApi.me() }.fold(
            onSuccess = { resumo ->
                _currentUser.value = resumo.toUser()
                true
            },
            onFailure = {
                clear()
                false
            }
        )
    }

    fun saveLoginTokens(accessToken: String, refreshToken: String, expiresAtIso: String) {
        val expires = runCatching { Instant.parse(expiresAtIso).toEpochMilli() }
            .getOrDefault(System.currentTimeMillis() + 15 * 60_000)
        tokenStore.saveTokens(accessToken, refreshToken, expires)
    }
}
