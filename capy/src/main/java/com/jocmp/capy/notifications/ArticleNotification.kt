package com.jocmp.capy.notifications

data class ArticleNotification(
    val id: String,
    val title: String,
    val feedID: String,
    val feedTitle: String,
    val feedFaviconURL: String?,
) {
    val notificationID
        get() = id.hashCode()
}
