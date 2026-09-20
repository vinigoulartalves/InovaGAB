package com.fiap.inovagab.core.network

import com.fiap.inovagab.BuildConfig
import com.fiap.inovagab.core.session.TokenStore
import com.fiap.inovagab.data.remote.api.AuthApi
import com.fiap.inovagab.data.remote.api.EstrategiasApi
import com.fiap.inovagab.data.remote.api.IdeiasApi
import com.fiap.inovagab.data.remote.api.ProjetosApi
import com.fiap.inovagab.data.remote.api.RankingApi
import com.fiap.inovagab.data.remote.api.RelatoriosApi
import com.fiap.inovagab.data.remote.api.UsuariosApi
import com.fiap.inovagab.data.remote.BigDecimalJsonAdapter
import com.fiap.inovagab.data.remote.dto.RefreshRequestDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlinx.coroutines.runBlocking

class NetworkModule(
    private val tokenStore: TokenStore,
    private val onSessionExpired: () -> Unit
) {

    val moshi: Moshi = Moshi.Builder()
        .add(BigDecimalJsonAdapter)
        .add(KotlinJsonAdapterFactory())
        .build()

    private val refreshLock = ReentrantLock()

    private val plainClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val refreshAuthApi: AuthApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(plainClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(AuthApi::class.java)

    private val authenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            if (response.code == 403) return null
            if (responseCount(response) >= 2) return null
            val path = response.request.url.encodedPath
            if (path.contains("/auth/login") || path.contains("/auth/refresh")) {
                tokenStore.clear()
                onSessionExpired()
                return null
            }

            val refreshed = refreshLock.withLock {
                val refresh = tokenStore.getRefreshToken() ?: return@withLock false
                runCatching {
                    val tokens = runBlocking {
                        refreshAuthApi.refresh(RefreshRequestDto(refresh))
                    }
                    val expires = runCatching {
                        java.time.Instant.parse(tokens.expiresAt).toEpochMilli()
                    }.getOrDefault(System.currentTimeMillis() + 15 * 60_000)
                    tokenStore.saveTokens(tokens.accessToken, tokens.refreshToken, expires)
                    true
                }.getOrElse {
                    tokenStore.clear()
                    onSessionExpired()
                    false
                }
            }

            if (!refreshed) return null
            val token = tokenStore.getAccessToken() ?: return null
            return response.request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        private fun responseCount(response: Response): Int {
            var count = 1
            var prior = response.priorResponse
            while (prior != null) {
                count++
                prior = prior.priorResponse
            }
            return count
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val token = tokenStore.getAccessToken()
        val request = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        redactHeader("Authorization")
    }

    private val apiClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .authenticator(authenticator)
        .addInterceptor(logging)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(apiClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val estrategiasApi: EstrategiasApi = retrofit.create(EstrategiasApi::class.java)
    val ideiasApi: IdeiasApi = retrofit.create(IdeiasApi::class.java)
    val projetosApi: ProjetosApi = retrofit.create(ProjetosApi::class.java)
    val relatoriosApi: RelatoriosApi = retrofit.create(RelatoriosApi::class.java)
    val rankingApi: RankingApi = retrofit.create(RankingApi::class.java)
    val usuariosApi: UsuariosApi = retrofit.create(UsuariosApi::class.java)
}
