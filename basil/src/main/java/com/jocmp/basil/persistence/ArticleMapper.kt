package com.jocmp.basil.persistence

import com.jocmp.basil.Article
import com.jocmp.basil.common.optionalURL
import com.jocmp.basil.common.toDateTimeFromSeconds
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

internal fun articleMapper(
    id: String,
    feedID: String?,
    title: String?,
    contentHtml: String?,
    url: String?,
    summary: String?,
    imageURL: String?,
    publishedAt: Long?,
    feedTitle: String?,
    faviconURL: String?,
    updatedAt: Long?,
    starred: Boolean?,
    read: Boolean?,
): Article {
    return Article(
        id = id,
        feedID = feedID.toString(),
        faviconURL = faviconURL,
        title = title ?: "",
        contentHTML = contentHtml ?: "",
        url = optionalURL(url),
        imageURL = optionalURL(imageURL),
        summary = summary ?: "",
        updatedAt = updatedAt!!.toDateTimeFromSeconds,
        publishedAt = publishedAt!!.toDateTimeFromSeconds,
        read = read ?: false,
        starred = starred ?: false,
        feedName = feedTitle ?: ""
    )
}

internal fun listMapper(
    id: String,
    feedID: String?,
    title: String?,
    contentHtml: String?,
    url: String?,
    summary: String?,
    imageURL: String?,
    publishedAt: Long?,
    feedTitle: String?,
    faviconURL: String?,
    updatedAt: Long?,
    starred: Boolean?,
    read: Boolean?,
): Article {
    return articleMapper(
        id = id,
        feedID = feedID.toString(),
        faviconURL = faviconURL,
        title = title ?: "",
        contentHtml = "",
        url = url,
        feedTitle = feedTitle,
        imageURL = imageURL,
        summary = summary ?: "",
        updatedAt = updatedAt,
        read = read ?: false,
        starred = starred ?: false,
        publishedAt = publishedAt
    )
}
