package com.jocmp.capy.persistence

import com.jocmp.capy.ArticleNotification

internal fun articleNotificationMapper(
    articleID: String,
    title: String?,
    summary: String?,
    feedID: String?,
    feedTitle: String?,
    feedFavicon: String?,
) = ArticleNotification(
    id = articleID.hashCode(),
    articleID = articleID,
    title = title.orEmpty().ifBlank { summary.orEmpty() },
    feedID = feedID!!,
    feedTitle = feedTitle.orEmpty(),
    feedFaviconURL = feedFavicon
)
