package com.jocmp.capy

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jocmp.capy.articles.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

fun Account.latestArticles(limit: Long = 30): Flow<List<Article>> {
    return articleRecords
        .byStatus
        .all(
            status = ArticleStatus.UNREAD,
            query = null,
            since = null,
            limit = limit,
            sortOrder = SortOrder.NEWEST_FIRST,
            offset = 0,
        )
        .asFlow()
        .mapToList(Dispatchers.IO)
}
