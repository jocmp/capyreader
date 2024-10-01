package com.capyreader.app.ui.settings

import com.capyreader.app.R

enum class ArticleVerticalSwipe {
    DISABLED,
    PREVIOUS_ARTICLE,
    NEXT_ARTICLE,
    LOAD_FULL_CONTENT;

    val translationKey: Int
        get() = when (this) {
            DISABLED -> R.string.article_vertical_swipe_disabled
            PREVIOUS_ARTICLE -> R.string.article_vertical_swipe_previous_article
            NEXT_ARTICLE -> R.string.article_vertical_swipe_next_article
            LOAD_FULL_CONTENT -> R.string.article_vertical_swipe_full_content
        }

    val enabled: Boolean
        get() = this != DISABLED

    val openArticle: Boolean
        get() = this == PREVIOUS_ARTICLE || this == NEXT_ARTICLE

    companion object {
        val topOptions = listOf(
            DISABLED,
            PREVIOUS_ARTICLE,
            LOAD_FULL_CONTENT,
        )

        val bottomOptions = listOf(
            DISABLED,
            NEXT_ARTICLE,
        )
    }
}
