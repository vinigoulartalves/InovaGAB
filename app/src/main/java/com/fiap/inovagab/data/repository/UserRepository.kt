package com.fiap.inovagab.data.repository

import com.fiap.inovagab.core.network.ApiCallRunner
import com.fiap.inovagab.data.model.User
import com.fiap.inovagab.data.remote.api.RankingApi
import com.fiap.inovagab.data.remote.toUser

class UserRepository(
    private val rankingApi: RankingApi
) {

    suspend fun listarParaRanking(): Result<List<User>> = ApiCallRunner.run {
        rankingApi.ranking().items.map { it.toUser() }
    }
}
