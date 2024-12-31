package com.capyreader.app.common

import com.capyreader.app.R

enum class AfterReadAllBehavior {
    NOTHING,
    HIDE_ARTICLES,
    OPEN_NEXT_FEED;

    val translationKey: Int
        get() = when (this) {
            NOTHING -> R.string.after_read_all_behavior_do_nothing
            HIDE_ARTICLES -> R.string.after_read_all_behavior_hide_articles
            OPEN_NEXT_FEED -> R.string.after_read_all_behavior_open_next_feed
        }

    companion object {
        fun withPreviousPref(openNextFeedOnReadAll: Boolean): AfterReadAllBehavior {
            return if (openNextFeedOnReadAll) {
                OPEN_NEXT_FEED
            } else {
                NOTHING
            }
        }
    }
}
