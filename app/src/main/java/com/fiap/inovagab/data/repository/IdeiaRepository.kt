package com.fiap.inovagab.data.repository

import com.fiap.inovagab.data.model.Ideia
import com.fiap.inovagab.data.model.StatusIdeia
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class IdeiaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val collection = firestore.collection("ideias")

    suspend fun listarTodas(): List<Ideia> {
        val snap = collection
            .orderBy("criadoEm", Query.Direction.DESCENDING)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(Ideia::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun listarPorAutor(autorUid: String): List<Ideia> {
        val snap = collection
            .whereEqualTo("autorUid", autorUid)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(Ideia::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun criar(ideia: Ideia): Result<String> = runCatching {
        val ref = collection.add(ideia).await()
        ref.id
    }

    suspend fun atualizarStatus(ideiaId: String, status: StatusIdeia): Result<Unit> = runCatching {
        collection.document(ideiaId).update("status", status.name).await()
        Unit
    }
}
