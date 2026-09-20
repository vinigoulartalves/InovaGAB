package com.fiap.inovagab.data.remote

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type
import java.math.BigDecimal

object BigDecimalJsonAdapter : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (Types.getRawType(type) != BigDecimal::class.java) return null
        return object : JsonAdapter<BigDecimal>() {
            override fun fromJson(reader: JsonReader): BigDecimal? {
                return when (reader.peek()) {
                    JsonReader.Token.NULL -> {
                        reader.nextNull<Unit>()
                        null
                    }
                    JsonReader.Token.NUMBER, JsonReader.Token.STRING ->
                        BigDecimal(reader.nextString())
                    else -> throw IllegalArgumentException("BigDecimal inválido no JSON")
                }
            }

            override fun toJson(writer: JsonWriter, value: BigDecimal?) {
                if (value == null) {
                    writer.nullValue()
                } else {
                    writer.value(value)
                }
            }
        }.nullSafe()
    }
}
