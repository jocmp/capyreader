package com.jocmp.capy.persistence

import com.jocmp.capy.Article
import com.jocmp.capy.common.optionalURL
import com.jocmp.capy.common.toDateTimeFromSeconds

internal fun articleMapper(
    id: String,
    feedID: String?,
    title: String?,
    author: String?,
    contentHtml: String?,
    extractedContentURL: String? = null,
    url: String?,
    summary: String?,
    imageURL: String?,
    publishedAt: Long?,
    feedTitle: String?,
    faviconURL: String?,
    enableStickyContent: Boolean,
    openInBrowser: Boolean,
    feedURL: String?,
    siteURL: String?,
    updatedAt: Long?,
    starred: Boolean,
    read: Boolean,
): Article {
    return Article(
        id = id,
        feedID = feedID.toString(),
        faviconURL = faviconURL,
        title = title ?: "",
        author = author,
        contentHTML = contentHtml ?: "",
        url = optionalURL(url),
        imageURL = imageURL,
        feedURL = feedURL,
        siteURL = siteURL,
        summary = summary ?: "",
        updatedAt = updatedAt!!.toDateTimeFromSeconds,
        publishedAt = publishedAt!!.toDateTimeFromSeconds,
        read = read,
        starred = starred,
        feedName = feedTitle ?: "",
        enableStickyFullContent = enableStickyContent,
        openInBrowser = openInBrowser
    )
}

internal fun listMapper(
    id: String,
    feedID: String?,
    title: String?,
    author: String?,
    url: String?,
    summary: String?,
    imageURL: String?,
    publishedAt: Long?,
    feedTitle: String?,
    faviconURL: String?,
    openInBrowser: Boolean,
    updatedAt: Long?,
    starred: Boolean?,
    read: Boolean?,
): Article {
    return articleMapper(
        id = id,
        feedID = feedID.toString(),
        faviconURL = faviconURL,
        title = title ?: "",
        author = author,
        contentHtml = "",
        enableStickyContent = false,
        feedURL = null,
        siteURL = null,
        url = url,
        feedTitle = feedTitle,
        imageURL = imageURL,
        summary = if (!summary.isNullOrBlank()) {
            summary
        } else if (title.isNullOrBlank()) {
            url.orEmpty()
        } else {
            ""
        },
        updatedAt = updatedAt,
        read = read ?: false,
        starred = starred ?: false,
        publishedAt = publishedAt,
        openInBrowser = openInBrowser ?: false,
    )
}
