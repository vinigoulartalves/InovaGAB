package com.fiap.inovagab.data.repository

import com.fiap.inovagab.core.network.ApiCallRunner
import com.fiap.inovagab.core.session.SessionManager
import com.fiap.inovagab.core.session.TokenStore
import com.fiap.inovagab.data.model.User
import com.fiap.inovagab.data.remote.api.AuthApi
import com.fiap.inovagab.data.remote.dto.LoginRequestDto
import com.fiap.inovagab.data.remote.dto.LogoutRequestDto
import com.fiap.inovagab.data.remote.toUser

class AuthRepository(
    private val sessionManager: SessionManager,
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) {

    suspend fun login(email: String, senha: String): Result<User> = ApiCallRunner.run {
        val response = authApi.login(LoginRequestDto(email.trim(), senha))
        sessionManager.saveLoginTokens(
            response.accessToken,
            response.refreshToken,
            response.expiresAt
        )
        val user = response.usuario.toUser()
        sessionManager.setUser(user)
        user
    }

    suspend fun logout(): Result<Unit> = ApiCallRunner.run {
        val refresh = tokenStore.getRefreshToken()
        if (!refresh.isNullOrBlank()) {
            runCatching { authApi.logout(LogoutRequestDto(refresh)) }
        }
        sessionManager.clear()
    }
}
