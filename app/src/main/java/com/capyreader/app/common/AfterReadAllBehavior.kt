package com.capyreader.app.common

import com.capyreader.app.R

enum class AfterReadAllBehavior {
    NOTHING,
    OPEN_DRAWER,
    OPEN_NEXT_FEED;

    val translationKey: Int
        get() = when (this) {
            NOTHING -> R.string.after_read_all_behavior_default
            OPEN_DRAWER -> R.string.after_read_all_behavior_open_drawer
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
