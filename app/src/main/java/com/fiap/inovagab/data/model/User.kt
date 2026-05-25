package com.fiap.inovagab.data.model

enum class UserRole {
    OPERADOR,
    GESTOR,
    LIDER
}

data class User(
    val uid: String = "",
    val nome: String = "",
    val email: String = "",
    val role: UserRole = UserRole.OPERADOR,
    val area: String = "",
    val pontos: Int = 0
)
