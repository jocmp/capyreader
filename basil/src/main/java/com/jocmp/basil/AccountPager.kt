package com.jocmp.basil

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.jocmp.basil.persistence.ArticlePagerFactory
import java.time.ZonedDateTime

fun Account.buildPager(
    filter: ArticleFilter,
    since: ZonedDateTime = ZonedDateTime.now()
): Pager<Int, Article> {
    return Pager(
        config = PagingConfig(pageSize = 10),
        pagingSourceFactory = { ArticlePagerFactory(database).find(filter, since = since) }
    )
}
