package com.jocmp.basil.persistence

import com.jocmp.basil.ArticleStatus

internal data class ArticleStatusPair(
    val read: Boolean?,
    val starred: Boolean?
)

internal val ArticleStatus.toStatusPair: ArticleStatusPair
    get() = when(this) {
        ArticleStatus.ALL -> ArticleStatusPair(read = null, starred = null)
        ArticleStatus.UNREAD -> ArticleStatusPair(read = false, starred = null)
        ArticleStatus.BOOKMARKED -> ArticleStatusPair(read = null, starred = true)
    }
