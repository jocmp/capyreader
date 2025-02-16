package com.capyreader.app.preferences

import com.capyreader.app.R

enum class ArticleListVerticalSwipe {
    DISABLED,
    NEXT_FEED;

    val translationKey: Int
        get() = when (this) {
            DISABLED -> R.string.article_list_swipe_disabled
            NEXT_FEED -> R.string.article_list_swipe_next_feed
        }

    companion object {
        val default = NEXT_FEED
    }
}
