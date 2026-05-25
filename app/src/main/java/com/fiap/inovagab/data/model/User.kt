package com.fiap.inovagab.data.model

enum class Perfil {
    OPERADOR,
    GESTOR,
    LIDER
}

data class User(
    val uid: String = "",
    val nome: String = "",
    val email: String = "",
    val perfil: Perfil = Perfil.OPERADOR,
    val pontos: Int = 0
)
