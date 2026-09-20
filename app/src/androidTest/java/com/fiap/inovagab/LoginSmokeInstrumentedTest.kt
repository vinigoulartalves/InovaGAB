package com.fiap.inovagab

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.URL

/**
 * Requer API real acessível (emulador: 10.0.2.2:8080). Sem backend, o teste é ignorado (não falha falso verde de UI).
 */
@RunWith(AndroidJUnit4::class)
class LoginSmokeInstrumentedTest {

    @Test
    fun health_ready_responde_quando_api_disponivel() {
        val baseUrl = InstrumentationRegistry.getArguments().getString("apiBaseUrl")
            ?: BuildConfig.API_BASE_URL
        val healthUrl = baseUrl.trimEnd('/') + "/health/ready"

        val reachable = runCatching {
            val conn = URL(healthUrl).openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "GET"
            val code = conn.responseCode
            conn.disconnect()
            code == 200
        }.getOrDefault(false)

        assumeTrue(
            "API indisponível em $healthUrl — suba backend e use emulador com 10.0.2.2:8080",
            reachable
        )
    }
}
