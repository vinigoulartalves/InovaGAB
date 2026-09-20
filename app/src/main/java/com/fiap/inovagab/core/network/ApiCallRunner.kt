package com.fiap.inovagab.core.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException

object ApiCallRunner {

    private val problemAdapter: JsonAdapter<ProblemBody> = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(ProblemBody::class.java)

    suspend fun <T> run(block: suspend () -> T): Result<T> = runCatching {
        block()
    }.recoverCatching { erro ->
        throw mapError(erro)
    }

    private fun mapError(erro: Throwable): Throwable {
        if (erro is ApiException) return erro
        if (erro is HttpException) {
            val body = erro.response()?.errorBody()?.string()
            val problem = body?.let { runCatching { problemAdapter.fromJson(it) }.getOrNull() }
            val code = problem?.code
            val detail = problem?.detail ?: erro.message() ?: "Erro na API"
            return ApiException(erro.code(), code, detail)
        }
        return erro
    }

    private data class ProblemBody(
        val detail: String? = null,
        val code: String? = null
    )
}
