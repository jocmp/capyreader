package com.jocmp.capy

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.persistence.ArticlePagerFactory
import java.time.OffsetDateTime

fun Account.buildArticlePager(
    filter: ArticleFilter,
    query: String? = null,
    sortOrder: SortOrder = SortOrder.NEWEST_FIRST,
    since: OffsetDateTime = OffsetDateTime.now()
): Pager<Int, Article> {
    return Pager(
        config = PagingConfig(
            pageSize = 50,
            prefetchDistance = 10,
        ),
        pagingSourceFactory = {
            ArticlePagerFactory(database).findArticles(
                filter = filter,
                query = query,
                sortOrder = sortOrder,
                since = since
            )
        }
    )
}
