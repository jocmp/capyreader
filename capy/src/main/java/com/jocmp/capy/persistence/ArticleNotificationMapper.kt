package com.jocmp.capy.persistence

import com.jocmp.capy.ArticleNotification

internal fun articleNotificationMapper(
    articleID: String,
    title: String?,
    summary: String?,
    url: String?,
    feedID: String?,
    feedTitle: String?,
    feedFavicon: String?,
    openInBrowser: Boolean,
) = ArticleNotification(
    id = articleID.hashCode(),
    articleID = articleID,
    title = title.orEmpty().ifBlank { summary.orEmpty() },
    url = url,
    feedID = feedID!!,
    feedTitle = feedTitle.orEmpty(),
    feedFaviconURL = feedFavicon,
    openInBrowser = openInBrowser,
)
