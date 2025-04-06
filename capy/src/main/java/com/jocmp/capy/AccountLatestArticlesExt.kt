package com.jocmp.capy

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jocmp.capy.articles.UnreadSortOrder
import kotlinx.coroutines.Dispatchers

val Account.latestArticles
    get() =
        articleRecords
            .byStatus
            .all(
                status = ArticleStatus.UNREAD,
                query = null,
                since = null,
                limit = 10,
                unreadSort = UnreadSortOrder.NEWEST_FIRST,
                offset = 0,
            )
            .asFlow()
            .mapToList(Dispatchers.IO)
