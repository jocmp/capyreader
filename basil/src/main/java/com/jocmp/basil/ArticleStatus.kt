package com.jocmp.basil

import kotlinx.serialization.Serializable

@Serializable
enum class ArticleStatus {
    ALL,
    UNREAD,
    STARRED
}
