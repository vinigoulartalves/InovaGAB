package com.fiap.inovagab.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiMessagesTest {

    @Test
    fun apiException_409_mensagem_concorrencia() {
        val ex = ApiException(409, "CONCORRENCIA", "detail bruto")
        assertTrue(ex.toUserMessage().contains("alterados por outro usuário"))
    }

    @Test
    fun apiException_503_preserva_mensagem() {
        val ex = ApiException(503, "IA_INDISPONIVEL", "Gemini quota exceeded")
        assertEquals("Gemini quota exceeded", ex.toUserMessage())
    }
}
