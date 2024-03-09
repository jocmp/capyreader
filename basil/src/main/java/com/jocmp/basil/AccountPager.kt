package com.jocmp.basil

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.jocmp.basil.persistence.ArticlePagerFactory
import java.time.OffsetDateTime
import java.time.ZonedDateTime

fun Account.buildPager(
    filter: ArticleFilter,
    since: OffsetDateTime = OffsetDateTime.now()
): Pager<Int, Article> {
    return Pager(
        config = PagingConfig(pageSize = 20, prefetchDistance = 1),
        pagingSourceFactory = { ArticlePagerFactory(database).find(filter = filter, since = since) }
    )
}
