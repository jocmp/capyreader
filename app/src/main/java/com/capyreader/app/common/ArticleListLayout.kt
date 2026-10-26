package com.capyreader.app.common

import com.capyreader.app.R

enum class ArticleListLayout {
    LIST,
    CARDS;

    val translationKey: Int
        get() = when (this) {
            LIST -> R.string.article_list_layout_list
            CARDS -> R.string.article_list_layout_cards
        }

    companion object {
        val default = LIST
    }
}
