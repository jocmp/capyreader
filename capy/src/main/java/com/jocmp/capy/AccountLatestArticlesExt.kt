package com.jocmp.capy

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jocmp.capy.persistence.listMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

fun Account.latestArticles(limit: Long = 30): Flow<List<Article>> {
    return database.articlesByStatusQueries
        .latestUnread(limit = limit, mapper = ::listMapper)
        .asFlow()
        .mapToList(Dispatchers.IO)
}
