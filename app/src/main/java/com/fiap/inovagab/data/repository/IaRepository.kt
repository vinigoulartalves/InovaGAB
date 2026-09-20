package com.fiap.inovagab.data.repository

import com.fiap.inovagab.core.network.ApiCallRunner
import com.fiap.inovagab.data.model.AnaliseIa
import com.fiap.inovagab.data.model.AnaliseIaResumo
import com.fiap.inovagab.data.model.PrioridadeIdeia
import com.fiap.inovagab.data.remote.api.IdeiasApi
import com.fiap.inovagab.data.remote.fetchAllPages
import com.fiap.inovagab.data.remote.parseInstantMillis
import com.fiap.inovagab.data.remote.toAnaliseIa

class IaRepository(
    private val ideiasApi: IdeiasApi
) {

    suspend fun solicitarAnalise(ideiaId: String): Result<AnaliseIa> = ApiCallRunner.run {
        ideiasApi.solicitarAnaliseIa(ideiaId).toAnaliseIa()
    }

    suspend fun listarResumos(ideiaId: String): Result<List<AnaliseIaResumo>> = ApiCallRunner.run {
        fetchAllPages { page, size -> ideiasApi.listarAnalisesIa(ideiaId, page, size) }
            .map { dto ->
                AnaliseIaResumo(
                    id = dto.id,
                    pontuacaoTotal = dto.pontuacaoTotal,
                    prioridadeSugerida = runCatching {
                        PrioridadeIdeia.valueOf(dto.prioridadeSugerida)
                    }.getOrDefault(PrioridadeIdeia.MEDIA),
                    desatualizada = dto.desatualizada,
                    criadoEm = parseInstantMillis(dto.criadoEm)
                )
            }
    }
}
