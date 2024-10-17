package com.capyreader.app.refresher

import com.jocmp.capy.notifications.ArticleNotification

internal data class FeedNotification(
    val id: String,
    val title: String,
    val faviconURL: String? = null,
    val notifications: List<ArticleNotification>
) {
    val articleCount: Int
        get() = notifications.size

    val notificationID
        get() = id.hashCode()

    companion object {
        fun from(feedID: String, notifications: List<ArticleNotification>): FeedNotification {
            val title = notifications.firstOrNull()?.feedTitle.orEmpty()
            val faviconURL = notifications.firstOrNull()?.feedFaviconURL

            return FeedNotification(
                id = feedID,
                title = title,
                faviconURL = faviconURL,
                notifications = notifications
            )
        }
    }
}
