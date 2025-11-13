package com.jocmp.capy.articles

enum class SortOrder  {
    NEWEST_FIRST,
    OLDEST_FIRST;

    companion object {
        val default = NEWEST_FIRST
    }
}
