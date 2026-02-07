package com.jocmp.capy.persistence.articles

import com.jocmp.capy.articles.SortOrder
import java.time.OffsetDateTime

internal fun isDescendingOrder(sortOrder: SortOrder) =
    sortOrder == SortOrder.NEWEST_FIRST

internal fun mapLastRead(read: Boolean?, value: OffsetDateTime?): Long? {
    if (read != null) {
        return value?.toEpochSecond()
    }

    return null
}

internal fun mapLastStarred(starred: Boolean?, value: OffsetDateTime?): Long? {
    if (starred != null) {
        return value?.toEpochSecond()
    }

    return null
}

