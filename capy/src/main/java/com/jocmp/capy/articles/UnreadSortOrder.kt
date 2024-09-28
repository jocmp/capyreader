package com.jocmp.capy.articles

enum class UnreadSortOrder  {
    NEWEST_FIRST,
    OLDEST_FIRST;

    companion object {
        val default = NEWEST_FIRST
    }
}
