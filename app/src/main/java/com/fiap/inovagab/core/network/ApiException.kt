package com.fiap.inovagab.core.network

class ApiException(
    val httpCode: Int,
    val code: String?,
    override val message: String
) : Exception(message)
