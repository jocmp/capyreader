package com.capyreader.app.ui.articles

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.articles.SortOrder
import java.time.OffsetDateTime

fun Account.buildArticlePager(
    filter: ArticleFilter,
    query: String? = null,
    sortOrder: SortOrder = SortOrder.NEWEST_FIRST,
    since: OffsetDateTime = OffsetDateTime.now()
): Pager<Long, Article> {
    val pageSize = 100

    return Pager(
        config = PagingConfig(
            pageSize = pageSize,
            initialLoadSize = pageSize,
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
