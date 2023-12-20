package com.jocmp.basil.extensions

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.cash.sqldelight.paging3.QueryPagingSource
import com.jocmp.basil.Account
import com.jocmp.basil.db.Articles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

fun Account.feedPagingSource(feedID: String?) = Pager(
    config = PagingConfig(pageSize = 10),
    pagingSourceFactory = {
        QueryPagingSource(
            countQuery = database.articlesQueries.countByFeed(feedID?.toLongOrNull()),
            transacter = database.articlesQueries,
            context = Dispatchers.IO,
            queryProvider = { limit, offset ->
                database.articlesQueries.allByFeed(
                    feedID = feedID?.toLongOrNull(),
                    limit = limit,
                    offset = offset
                )
            },
        )
    }
)
