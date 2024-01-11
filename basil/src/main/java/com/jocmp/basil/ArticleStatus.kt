package com.jocmp.basil

import kotlinx.serialization.Serializable

@Serializable
enum class ArticleStatus(value: String) {
    ALL("all"),
    UNREAD("unread"),
    STARRED("starred")
}
