package com.fiap.inovagab.data.remote

import com.fiap.inovagab.data.remote.dto.PagedResultDto

suspend fun <T> fetchAllPages(
    pageSize: Int = 50,
    loader: suspend (page: Int, pageSize: Int) -> PagedResultDto<T>
): List<T> {
    val acumulado = mutableListOf<T>()
    var page = 1
    var totalPages = 1
    while (page <= totalPages) {
        val result = loader(page, pageSize)
        acumulado.addAll(result.items)
        totalPages = result.totalPages.coerceAtLeast(1)
        if (result.items.isEmpty()) break
        page++
    }
    return acumulado
}
