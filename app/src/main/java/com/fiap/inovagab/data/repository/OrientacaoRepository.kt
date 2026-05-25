package com.fiap.inovagab.data.repository

import com.fiap.inovagab.data.model.Orientacao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OrientacaoRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val collection = firestore.collection("orientacoes")

    suspend fun listar(): List<Orientacao> {
        val snap = collection
            .orderBy("criadoEm", Query.Direction.DESCENDING)
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            doc.toObject(Orientacao::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun criar(orientacao: Orientacao): Result<String> = runCatching {
        val ref = collection.add(orientacao).await()
        ref.id
    }
}
