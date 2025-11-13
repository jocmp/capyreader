package com.jocmp.capy.persistence.articles

import com.jocmp.capy.articles.SortOrder

fun isNewestFirst(sortOrder: SortOrder): Boolean {
    return sortOrder == SortOrder.NEWEST_FIRST
}
