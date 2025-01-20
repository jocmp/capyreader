package com.jocmp.capy.persistence

import com.jocmp.capy.ArticleNotification

internal fun articleNotificationMapper(
    articleID: String,
    title: String?,
    feedID: String?,
    feedTitle: String?,
    feedFavicon: String?,
) = ArticleNotification(
    id = articleID.hashCode(),
    articleID = articleID,
    title = title.orEmpty(),
    feedID = feedID!!,
    feedTitle = feedTitle.orEmpty(),
    feedFaviconURL = feedFavicon
)
