package com.jocmp.capy

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.jocmp.capy.articles.UnreadSortOrder
import com.jocmp.capy.persistence.ArticlePagerFactory
import java.time.OffsetDateTime

fun Account.buildArticlePager(
    filter: ArticleFilter,
    query: String? = null,
    unreadSort: UnreadSortOrder,
    since: OffsetDateTime = OffsetDateTime.now()
): Pager<Int, Article> {
    return Pager(
        config = PagingConfig(
            pageSize = 50,
            prefetchDistance = 10,
        ),
        pagingSourceFactory = {
            ArticlePagerFactory(database).find(
                filter = filter,
                query = query,
                unreadSort = unreadSort,
                since = since
            )
        }
    )
}
