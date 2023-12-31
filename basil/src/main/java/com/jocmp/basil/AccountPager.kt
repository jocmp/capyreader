package com.jocmp.basil

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import com.jocmp.basil.persistence.ArticlePagerFactory

fun Account.buildPager(filter: ArticleFilter): Pager<Int, Article> {
    return Pager(
        config = PagingConfig(pageSize = 10),
        pagingSourceFactory = { ArticlePagerFactory(database).find(filter) }
    )
}
