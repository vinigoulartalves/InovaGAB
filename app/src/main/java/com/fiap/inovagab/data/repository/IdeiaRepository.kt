package com.fiap.inovagab.data.repository

import com.fiap.inovagab.data.model.Ideia
import com.fiap.inovagab.data.model.PrioridadeIdeia
import com.fiap.inovagab.data.model.StatusIdeia
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class IdeiaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val ideiasCollection = firestore.collection("ideias")
    private val usersCollection = firestore.collection("users")

    suspend fun listarTodas(): Result<List<Ideia>> = runCatching {
        val snap = ideiasCollection.get().await()
        snap.documents
            .mapNotNull { doc -> doc.toObject(Ideia::class.java)?.copy(id = doc.id) }
            .sortedByDescending { it.criadoEm }
    }

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

    suspend fun atualizarPrioridade(
        ideiaId: String,
        prioridade: PrioridadeIdeia
    ): Result<Unit> = runCatching {
        require(ideiaId.isNotBlank()) { "ID da ideia é obrigatório." }
        ideiasCollection.document(ideiaId).update("prioridade", prioridade.name).await()
        Unit
    }

    /**
     * Atualiza o status de uma ideia e, quando o novo status for [StatusIdeia.APROVADA] e a
     * ideia ainda não estiver aprovada, soma [PONTOS_POR_APROVACAO] pontos ao autor.
     * Isso evita pontuar novamente caso a ideia já estivesse aprovada.
     */
    suspend fun atualizarStatusComPontuacao(
        ideia: Ideia,
        novoStatus: StatusIdeia
    ): Result<Unit> = runCatching {
        require(ideia.id.isNotBlank()) { "ID da ideia é obrigatório." }

        ideiasCollection.document(ideia.id).update("status", novoStatus.name).await()

        val deveSomarPontos = novoStatus == StatusIdeia.APROVADA &&
            ideia.status != StatusIdeia.APROVADA &&
            ideia.autorId.isNotBlank()

        if (deveSomarPontos) {
            runCatching {
                usersCollection.document(ideia.autorId)
                    .update("pontos", FieldValue.increment(PONTOS_POR_APROVACAO))
                    .await()
            }
        }
        Unit
    }

    companion object {
        const val PONTOS_POR_IDEIA: Long = 10
        const val PONTOS_POR_APROVACAO: Long = 30
    }
}
