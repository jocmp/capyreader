package com.jocmp.basil.persistence

import com.jocmp.basil.Article
import com.jocmp.basil.shared.optionalURL
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

internal fun articleMapper(
    id: Long,
    externalID: String,
    feedID: Long?,
    title: String?,
    contentHtml: String?,
    url: String?,
    summary: String?,
    imageURL: String?,
    publishedAt: Long?,
    arrivedAt: Long?,
    read: Boolean?,
    starred: Boolean?,
    zoneID: ZoneId = ZoneId.systemDefault()
): Article {
    return Article(
        id = id.toString(),
        externalID = externalID,
        feedID = feedID.toString(),
        title = title ?: "",
        contentHTML = contentHtml ?: "",
        url = optionalURL(url),
        imageURL = optionalURL(imageURL),
        summary = summary ?: "",
        arrivedAt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(arrivedAt!!), zoneID)
    )
}
