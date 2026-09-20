package com.fiap.inovagab.core.network

fun ApiException.toUserMessage(): String = when (httpCode) {
    409 -> "Os dados foram alterados por outro usuário. Atualize a tela e tente novamente."
    503 -> message.ifBlank { "Serviço de IA indisponível no momento." }
    else -> message
}

fun Throwable.toUserMessage(): String = when (this) {
    is ApiException -> toUserMessage()
    else -> message ?: "Ocorreu um erro inesperado."
}
