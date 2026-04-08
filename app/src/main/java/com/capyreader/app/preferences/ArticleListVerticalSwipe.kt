package com.capyreader.app.preferences

import com.capyreader.app.R

enum class ArticleListVerticalSwipe {
    DISABLED,
    NEXT_FEED,
    MARK_ALL_READ;

    val translationKey: Int
        get() = when (this) {
            DISABLED -> R.string.article_list_swipe_disabled
            NEXT_FEED -> R.string.article_list_swipe_next_feed
            MARK_ALL_READ -> R.string.article_list_swipe_mark_all_read
        }

    companion object {
        val default = NEXT_FEED
    }
}
