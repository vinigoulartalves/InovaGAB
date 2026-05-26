package com.fiap.inovagab.data.repository

import com.fiap.inovagab.data.model.Orientacao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OrientacaoRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val collection = firestore.collection("orientacoes")

    suspend fun listar(): Result<List<Orientacao>> = runCatching {
        val snap = collection
            .orderBy("criadoEm", Query.Direction.DESCENDING)
            .get()
            .await()
        snap.documents.mapNotNull { doc ->
            doc.toObject(Orientacao::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun buscarPorId(id: String): Result<Orientacao?> = runCatching {
        val snap = collection.document(id).get().await()
        if (!snap.exists()) null
        else snap.toObject(Orientacao::class.java)?.copy(id = snap.id)
    }

    suspend fun criar(orientacao: Orientacao): Result<String> = runCatching {
        val dados = mapOf(
            "titulo" to orientacao.titulo,
            "descricao" to orientacao.descricao,
            "criadoEm" to orientacao.criadoEm
        )
        val ref = collection.add(dados).await()
        ref.id
    }

    suspend fun atualizar(orientacao: Orientacao): Result<Unit> = runCatching {
        require(orientacao.id.isNotBlank()) { "ID da orientação é obrigatório para atualização." }
        val dados = mapOf(
            "titulo" to orientacao.titulo,
            "descricao" to orientacao.descricao
        )
        collection.document(orientacao.id).update(dados).await()
        Unit
    }

    suspend fun excluir(id: String): Result<Unit> = runCatching {
        require(id.isNotBlank()) { "ID da orientação é obrigatório para exclusão." }
        collection.document(id).delete().await()
        Unit
    }
}
