package com.fiap.inovagab.data.repository

import com.fiap.inovagab.data.model.Projeto
import com.fiap.inovagab.data.model.StatusProjeto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ProjetoRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val collection = firestore.collection("projetos")

    suspend fun listar(): Result<List<Projeto>> = runCatching {
        val snap = collection
            .orderBy("criadoEm", Query.Direction.DESCENDING)
            .get()
            .await()
        snap.documents.mapNotNull { doc ->
            doc.toObject(Projeto::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun buscarPorId(id: String): Result<Projeto?> = runCatching {
        require(id.isNotBlank()) { "ID do projeto é obrigatório." }
        val snap = collection.document(id).get().await()
        if (!snap.exists()) null
        else snap.toObject(Projeto::class.java)?.copy(id = snap.id)
    }

    suspend fun criar(projeto: Projeto): Result<String> = runCatching {
        val dados = projetoParaMap(projeto)
        val ref = collection.add(dados).await()
        ref.id
    }

    suspend fun atualizar(projeto: Projeto): Result<Unit> = runCatching {
        require(projeto.id.isNotBlank()) { "ID do projeto é obrigatório para atualização." }
        val dados = projetoParaMap(projeto)
        collection.document(projeto.id).update(dados).await()
        Unit
    }

    private fun projetoParaMap(projeto: Projeto): Map<String, Any> = mapOf(
        "nome" to projeto.nome,
        "descricao" to projeto.descricao,
        "ideiaId" to projeto.ideiaId,
        "responsavel" to projeto.responsavel,
        "etapa" to projeto.etapa,
        "status" to (projeto.status.name.ifBlank { StatusProjeto.PLANEJADO.name }),
        "investimento" to projeto.investimento,
        "retornoFinanceiro" to projeto.retornoFinanceiro,
        "reducaoCustos" to projeto.reducaoCustos,
        "ganhoProdutividade" to projeto.ganhoProdutividade,
        "prazo" to projeto.prazo,
        "criadoEm" to projeto.criadoEm
    )
}
