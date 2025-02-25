package com.jocmp.capy.persistence.articles

import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.articles.UnreadSortOrder

fun isNewestFirst(status: ArticleStatus, unreadSort: UnreadSortOrder): Boolean {
    return status != ArticleStatus.UNREAD ||
            unreadSort == UnreadSortOrder.NEWEST_FIRST
}
