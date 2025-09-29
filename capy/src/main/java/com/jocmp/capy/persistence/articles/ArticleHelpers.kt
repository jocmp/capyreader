package com.jocmp.capy.persistence.articles

import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.articles.UnreadSortOrder
import java.time.OffsetDateTime

internal fun isDescendingOrder(status: ArticleStatus, unreadSort: UnreadSortOrder) =
    status != ArticleStatus.UNREAD ||
            unreadSort == UnreadSortOrder.NEWEST_FIRST

internal fun mapLastRead(read: Boolean?, value: OffsetDateTime?): Long? {
    if (read != null) {
        return value?.toEpochSecond()
    }

    return null
}

