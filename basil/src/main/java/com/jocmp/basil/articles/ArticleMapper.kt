package com.jocmp.basil.articles

import com.jocmp.basil.Article
import com.jocmp.basil.shared.optionalURL

internal fun articleMapper(
    id: Long,
    externalID: String,
    mapperFeedID: Long?,
    title: String?,
    contentHtml: String?,
    url: String?,
    summary: String?,
    imageURL: String?,
    datePublished: Long?
): Article {
    return Article(
        id = id.toString(),
        externalID = externalID,
        feedID = mapperFeedID.toString(),
        title = title ?: "",
        contentHTML = contentHtml ?: "",
        url = optionalURL(url),
        imageURL = optionalURL(imageURL),
        summary = summary ?: ""
    )
}
