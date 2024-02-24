package com.jocmp.basil.persistence

import com.jocmp.basil.Article
import com.jocmp.basil.common.optionalURL
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
    arrivedAt: Long?,
    starred: Boolean?,
    read: Boolean?,
    zoneID: ZoneId = ZoneId.systemDefault()
): Article {
    return Article(
        id = id,
        feedID = feedID.toString(),
        title = title ?: "",
        contentHTML = contentHtml ?: "",
        url = optionalURL(url),
        imageURL = optionalURL(imageURL),
        summary = summary ?: "",
        arrivedAt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(arrivedAt!!), zoneID),
        read = read ?: false,
        starred = starred ?: false
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
    arrivedAt: Long?,
    starred: Boolean?,
    read: Boolean?,
    zoneID: ZoneId = ZoneId.systemDefault()
): Article {
    return Article(
        id = id,
        feedID = feedID.toString(),
        title = title ?: "",
        contentHTML = "",
        url = optionalURL(url),
        imageURL = optionalURL(imageURL),
        summary = summary ?: "",
        arrivedAt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(arrivedAt!!), zoneID),
        read = read ?: false,
        starred = starred ?: false
    )
}
