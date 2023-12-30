package com.jocmp.basil

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.jocmp.basil.persistence.ArticlePagerFactory

fun Account.buildPager(
    filter: Filter = Filter.Articles(status = Filter.Status.ALL)
): Pager<Int, Article> {
    val pagerFactory = ArticlePagerFactory(
        database = database,
        filter = filter
    )

    return Pager(
        config = PagingConfig(pageSize = 10),
        pagingSourceFactory = { pagerFactory.find() }
    )
}
