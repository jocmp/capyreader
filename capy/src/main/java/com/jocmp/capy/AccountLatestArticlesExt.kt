package com.jocmp.capy

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jocmp.capy.articles.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

fun Account.latestArticles(limit: Long = 30): Flow<List<Article>> {
    val boundariesProvider = articleRecords
        .byStatus
        .pageBoundaries(status = ArticleStatus.UNREAD)

    val queryProvider = articleRecords
        .byStatus
        .keyed(
            status = ArticleStatus.UNREAD,
            sortOrder = SortOrder.NEWEST_FIRST,
        )

    val boundaries = boundariesProvider(null, limit).executeAsList()
    val begin = boundaries.firstOrNull() ?: return kotlinx.coroutines.flow.flowOf(emptyList())
    val end = boundaries.getOrNull(1)

    return queryProvider(begin, end)
        .asFlow()
        .mapToList(Dispatchers.IO)
}
