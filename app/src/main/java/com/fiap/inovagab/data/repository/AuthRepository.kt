package com.fiap.inovagab.data.repository

import com.fiap.inovagab.data.model.Perfil
import com.fiap.inovagab.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun login(email: String, senha: String): Result<User> = runCatching {
        val result = auth.signInWithEmailAndPassword(email.trim(), senha).await()
        val uid = result.user?.uid ?: error("Falha ao obter usuário autenticado.")
        carregarUsuario(uid) ?: error("Perfil de usuário não encontrado no Firestore.")
    }

    private suspend fun carregarUsuario(uid: String): User? {
        val snap = firestore.collection("users").document(uid).get().await()
        if (!snap.exists()) return null

        val perfilStr = snap.getString("perfil")?.trim()?.uppercase()
        val perfil = runCatching { Perfil.valueOf(perfilStr ?: "") }
            .getOrElse { error("Perfil inválido para o usuário.") }

        return User(
            uid = uid,
            nome = snap.getString("nome").orEmpty(),
            email = snap.getString("email").orEmpty(),
            perfil = perfil,
            pontos = (snap.getLong("pontos") ?: 0L).toInt()
        )
    }

    fun logout() {
        auth.signOut()
    }
}
