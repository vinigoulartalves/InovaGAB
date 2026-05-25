package com.fiap.inovagab.data.repository

import com.fiap.inovagab.data.model.Projeto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ProjetoRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val collection = firestore.collection("projetos")

    suspend fun listar(): List<Projeto> {
        val snap = collection
            .orderBy("criadoEm", Query.Direction.DESCENDING)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(Projeto::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun criar(projeto: Projeto): Result<String> = runCatching {
        val ref = collection.add(projeto).await()
        ref.id
    }
}
