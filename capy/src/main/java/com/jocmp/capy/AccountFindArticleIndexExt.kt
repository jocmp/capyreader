package com.jocmp.capy

import com.jocmp.capy.articles.UnreadSortOrder
import java.time.OffsetDateTime

fun Account.findArticleIndex(
    articleID: String,
    filter: ArticleFilter,
    query: String? = null,
    unreadSort: UnreadSortOrder = UnreadSortOrder.NEWEST_FIRST,
    since: OffsetDateTime = OffsetDateTime.now()
): Long {
    return articleRecords.findIndex(
        articleID = articleID,
        filter = filter,
        query = query,
        unreadSort = unreadSort,
        since = since
    )
}
