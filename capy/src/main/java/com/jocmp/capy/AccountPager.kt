package com.jocmp.capy

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.jocmp.capy.persistence.ArticlePagerFactory
import java.time.OffsetDateTime

fun Account.buildPager(
    filter: ArticleFilter,
    since: OffsetDateTime = OffsetDateTime.now()
): Pager<Int, Article> {
    return Pager(
        config = PagingConfig(
            pageSize = 20,
            prefetchDistance = 1,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { ArticlePagerFactory(database).find(filter = filter, since = since) }
    )
}
