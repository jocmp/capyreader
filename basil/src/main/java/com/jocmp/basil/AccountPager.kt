package com.jocmp.basil

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.jocmp.basil.persistence.ArticlePagerFactory

fun Account.buildPager(
    filter: ArticleFilter = ArticleFilter.Articles(status = ArticleStatus.ALL)
): Pager<Int, Article> {
    val pagerFactory = ArticlePagerFactory(database = database)

    return Pager(
        config = PagingConfig(pageSize = 10),
        pagingSourceFactory = { pagerFactory.find(filter) }
    )
}
