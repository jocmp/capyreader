package com.jocmp.capy

import kotlinx.serialization.Serializable

@Serializable
enum class ArticleStatus {
    ALL,
    UNREAD,
    STARRED
}
