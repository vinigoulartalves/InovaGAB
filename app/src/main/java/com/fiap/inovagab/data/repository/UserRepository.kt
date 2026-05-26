package com.fiap.inovagab.data.repository

import com.fiap.inovagab.data.model.Perfil
import com.fiap.inovagab.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val collection = firestore.collection("users")

    /**
     * Lista todos os usuários cadastrados ordenados por pontos em ordem decrescente.
     * A ordenação é feita em memória para tolerar documentos sem o campo "pontos".
     */
    suspend fun listarParaRanking(): Result<List<User>> = runCatching {
        val snap = collection.get().await()
        snap.documents
            .mapNotNull { doc ->
                val perfilStr = doc.getString("perfil")?.trim()?.uppercase()
                val perfil = runCatching { Perfil.valueOf(perfilStr ?: "") }.getOrNull()
                    ?: return@mapNotNull null

                User(
                    uid = doc.id,
                    nome = doc.getString("nome").orEmpty(),
                    email = doc.getString("email").orEmpty(),
                    perfil = perfil,
                    pontos = (doc.getLong("pontos") ?: 0L).toInt()
                )
            }
            .sortedWith(
                compareByDescending<User> { it.pontos }
                    .thenBy { it.nome.lowercase() }
            )
    }
}
