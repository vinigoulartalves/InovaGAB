package com.fiap.inovagab.data.repository

import com.fiap.inovagab.data.model.Ideia
import com.fiap.inovagab.data.model.PrioridadeIdeia
import com.fiap.inovagab.data.model.StatusIdeia
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class IdeiaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val ideiasCollection = firestore.collection("ideias")
    private val usersCollection = firestore.collection("users")

    suspend fun listarPorAutor(autorId: String): Result<List<Ideia>> = runCatching {
        require(autorId.isNotBlank()) { "ID do autor é obrigatório." }
        val snap = ideiasCollection
            .whereEqualTo("autorId", autorId)
            .get()
            .await()
        snap.documents
            .mapNotNull { doc -> doc.toObject(Ideia::class.java)?.copy(id = doc.id) }
            .sortedByDescending { it.criadoEm }
    }

    suspend fun criar(ideia: Ideia): Result<String> = runCatching {
        require(ideia.autorId.isNotBlank()) { "ID do autor é obrigatório para criar a ideia." }

        val dados = mapOf(
            "titulo" to ideia.titulo,
            "descricao" to ideia.descricao,
            "area" to ideia.area,
            "autorId" to ideia.autorId,
            "autorNome" to ideia.autorNome,
            "status" to (ideia.status.name.ifBlank { StatusIdeia.ENVIADA.name }),
            "prioridade" to (ideia.prioridade.name.ifBlank { PrioridadeIdeia.MEDIA.name }),
            "criadoEm" to ideia.criadoEm
        )

        val ref = ideiasCollection.add(dados).await()

        runCatching {
            usersCollection.document(ideia.autorId)
                .update("pontos", FieldValue.increment(PONTOS_POR_IDEIA))
                .await()
        }

        ref.id
    }

    suspend fun atualizarStatus(ideiaId: String, status: StatusIdeia): Result<Unit> = runCatching {
        require(ideiaId.isNotBlank()) { "ID da ideia é obrigatório." }
        ideiasCollection.document(ideiaId).update("status", status.name).await()
        Unit
    }

    companion object {
        const val PONTOS_POR_IDEIA: Long = 10
    }
}
