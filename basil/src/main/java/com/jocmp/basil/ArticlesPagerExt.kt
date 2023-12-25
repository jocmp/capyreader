package com.jocmp.basil

import androidx.paging.Pager
import androidx.paging.PagingConfig
import app.cash.sqldelight.paging3.QueryPagingSource
import com.jocmp.basil.articles.articleMapper
import kotlinx.coroutines.Dispatchers

fun Account.feedPagingSource(feedID: String?): Pager<Int, Article> {
    return Pager(
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
                        offset = offset,
                        mapper = ::articleMapper
                    )
                }
            )
        }
    )
}
