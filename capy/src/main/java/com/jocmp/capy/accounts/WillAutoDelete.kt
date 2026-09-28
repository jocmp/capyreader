package com.jocmp.capy.accounts

import java.time.ZonedDateTime

internal fun willAutoDelete(
    publishedAt: Long,
    read: Boolean,
    starred: Boolean,
    cutoffDate: ZonedDateTime?,
): Boolean {
    if (cutoffDate == null || !read || starred) {
        return false
    }

    return publishedAt < cutoffDate.toEpochSecond()
}
