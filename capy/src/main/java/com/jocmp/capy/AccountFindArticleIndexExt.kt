package com.jocmp.capy

import com.jocmp.capy.articles.UnreadSortOrder
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

fun Account.findArticlePages(
    articleID: String,
    filter: ArticleFilter,
    query: String? = null,
    unreadSort: UnreadSortOrder = UnreadSortOrder.NEWEST_FIRST,
    since: OffsetDateTime = OffsetDateTime.now()
): Flow<ArticlePages?> {
    return articleRecords
        .findPages(
            articleID = articleID,
            filter = filter,
            query = query,
            unreadSort = unreadSort,
            since = since
        )
}
