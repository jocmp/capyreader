package com.jocmp.capy.persistence

import com.jocmp.capy.Article
import com.jocmp.capy.EnclosureType
import com.jocmp.capy.common.optionalURL
import com.jocmp.capy.common.toDateTimeFromSeconds

internal fun articleMapper(
    id: String,
    feedID: String?,
    title: String?,
    author: String?,
    contentHtml: String?,
    extractedContentURL: String?,
    url: String?,
    summary: String?,
    imageURL: String?,
    publishedAt: Long?,
    enclosureType: String?,
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
        openInBrowser = openInBrowser,
        enclosureType = EnclosureType.from(enclosureType),
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
    enclosureType: String?,
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
        title = title ?: "",
        author = author,
        contentHtml = "",
        extractedContentURL = null,
        url = url,
        summary = if (!summary.isNullOrBlank()) {
            summary
        } else if (title.isNullOrBlank()) {
            url.orEmpty()
        } else {
            ""
        },
        imageURL = imageURL,
        publishedAt = publishedAt,
        enclosureType = enclosureType,
        feedTitle = feedTitle,
        faviconURL = faviconURL,
        enableStickyContent = false,
        openInBrowser = openInBrowser,
        feedURL = null,
        siteURL = null,
        updatedAt = updatedAt,
        starred = starred ?: false,
        read = read ?: false,
    )
}
