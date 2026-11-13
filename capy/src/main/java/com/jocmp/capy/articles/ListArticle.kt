package com.jocmp.capy.articles

import com.jocmp.capy.Article
import com.jocmp.capy.common.toDateTimeFromSeconds
import com.jocmp.capy.persistence.listMapper
import java.time.ZonedDateTime

class ArticleMembership(
    val id: String,
    val read: Boolean,
    val starred: Boolean,
)

class ListFeed(
    val title: String?,
    val faviconURL: String?,
    val openInBrowser: Boolean,
)

class ListArticle(
    val id: String,
    val feedID: String?,
    val title: String?,
    val url: String?,
    val summary: String?,
    val imageURL: String?,
    val publishedAt: Long?,
    val enclosureType: String?,
    val updatedAt: Long?,
) {
    val publishedAtDateTime: ZonedDateTime?
        get() = publishedAt?.toDateTimeFromSeconds

    fun toArticle(read: Boolean, starred: Boolean, feed: ListFeed?): Article {
        return listMapper(
            id = id,
            feedID = feedID,
            title = title,
            author = null,
            url = url,
            summary = summary,
            imageURL = imageURL,
            publishedAt = publishedAt,
            enclosureType = enclosureType,
            feedTitle = feed?.title,
            faviconURL = feed?.faviconURL,
            openInBrowser = feed?.openInBrowser ?: false,
            updatedAt = updatedAt,
            starred = starred,
            read = read,
        )
    }
}
