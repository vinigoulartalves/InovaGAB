package com.fiap.inovagab.data.repository

import com.fiap.inovagab.data.model.User
import com.fiap.inovagab.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun login(email: String, senha: String): Result<User> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, senha).await()
        val uid = result.user?.uid ?: error("Falha ao obter usuário")
        carregarUsuario(uid) ?: error("Usuário não encontrado no Firestore")
    }

    suspend fun cadastrar(
        nome: String,
        email: String,
        senha: String,
        role: UserRole,
        area: String
    ): Result<User> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, senha).await()
        val uid = result.user?.uid ?: error("Falha ao criar usuário")
        val user = User(
            uid = uid,
            nome = nome,
            email = email,
            role = role,
            area = area,
            pontos = 0
        )
        firestore.collection("usuarios").document(uid).set(user).await()
        user
    }

    suspend fun carregarUsuario(uid: String): User? {
        val snap = firestore.collection("usuarios").document(uid).get().await()
        return snap.toObject(User::class.java)
    }

    fun logout() {
        auth.signOut()
    }
}
