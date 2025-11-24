package com.capyreader.app.preferences

import com.capyreader.app.R

enum class ArticleVerticalSwipe {
    DISABLED,
    PREVIOUS_ARTICLE,
    NEXT_ARTICLE;

    val translationKey: Int
        get() = when (this) {
            DISABLED -> R.string.article_vertical_swipe_disabled
            PREVIOUS_ARTICLE -> R.string.article_vertical_swipe_previous_article
            NEXT_ARTICLE -> R.string.article_vertical_swipe_next_article
        }

    val enabled: Boolean
        get() = this != DISABLED

    val openArticle: Boolean
        get() = this == PREVIOUS_ARTICLE || this == NEXT_ARTICLE

    companion object {
        val topOptions = listOf(
            DISABLED,
            PREVIOUS_ARTICLE,
        )

        val bottomOptions = listOf(
            DISABLED,
            NEXT_ARTICLE,
        )

        val topSwipeDefault = PREVIOUS_ARTICLE

        val bottomSwipeDefault = NEXT_ARTICLE
    }
}
