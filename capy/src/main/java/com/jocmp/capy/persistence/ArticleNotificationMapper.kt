package com.jocmp.capy.persistence

import com.jocmp.capy.notifications.ArticleNotification

internal fun articleNotificationMapper(
    id: String,
    title: String?,
    feedID: String?,
    feedTitle: String?,
    feedFavicon: String?,
) = ArticleNotification(
    id = id,
    title = title.orEmpty(),
    feedID = feedID!!,
    feedTitle = feedTitle.orEmpty(),
    feedFaviconURL = feedFavicon
)
