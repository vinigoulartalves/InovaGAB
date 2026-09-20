package com.fiap.inovagab.data.remote

import com.fiap.inovagab.data.remote.dto.ProjetoResumoDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class MoneyMoshiTest {

    private val moshi = Moshi.Builder()
        .add(BigDecimalJsonAdapter)
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun deserialize_decimal_fields_as_big_decimal() {
        val json = """
            {
              "id": "p1",
              "nome": "P",
              "status": "PLANEJADO",
              "estrategiaId": "e1",
              "responsavelId": "g1",
              "investimento": 1000.5,
              "retornoFinanceiro": 1500,
              "criadoEm": "2026-09-20T12:00:00Z"
            }
        """.trimIndent()

        val dto = moshi.adapter(ProjetoResumoDto::class.java).fromJson(json)!!
        assertEquals(BigDecimal("1000.5"), dto.investimento)
        assertEquals(BigDecimal("1500"), dto.retornoFinanceiro)
    }
}
