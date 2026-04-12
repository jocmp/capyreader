package com.jocmp.capy.persistence

import com.jocmp.capy.ArticleStatus

internal data class ArticleStatusPair(
    val read: Boolean?,
    val starred: Boolean?
)

internal val ArticleStatus.toStatusPair: ArticleStatusPair
    get() = when(this) {
        ArticleStatus.ALL -> ArticleStatusPair(read = null, starred = null)
        ArticleStatus.UNREAD -> ArticleStatusPair(read = false, starred = null)
        ArticleStatus.STARRED -> ArticleStatusPair(read = null, starred = true)
    }

internal val ArticleStatus.forCounts: ArticleStatusPair
    get() = when(this) {
        ArticleStatus.STARRED -> ArticleStatusPair(read = null, starred = true)
        else -> ArticleStatusPair(read = false, starred = null)
    }
