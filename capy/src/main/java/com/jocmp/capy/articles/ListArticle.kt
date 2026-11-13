package com.jocmp.capy.articles

import com.jocmp.capy.Article
import com.jocmp.capy.persistence.listMapper

/** An article's place in a list and its status, without the row's content. */
class ArticleMembership(
    val id: String,
    val read: Boolean,
    val starred: Boolean,
)

/**
 * The fields an article list row shows, kept in memory for a whole list. Lighter than [Article]:
 * no HTML, timestamps stay epoch seconds, and URLs stay strings until [toArticle] builds a row.
 */
class ListArticle(
    val id: String,
    val feedID: String?,
    val title: String?,
    val url: String?,
    val summary: String?,
    val imageURL: String?,
    val publishedAt: Long?,
    val enclosureType: String?,
    val feedTitle: String?,
    val faviconURL: String?,
    val openInBrowser: Boolean,
    val updatedAt: Long?,
) {
    /** Builds the [Article] a list row renders, with the row's current status. */
    fun toArticle(read: Boolean, starred: Boolean): Article {
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
            feedTitle = feedTitle,
            faviconURL = faviconURL,
            openInBrowser = openInBrowser,
            updatedAt = updatedAt,
            starred = starred,
            read = read,
        )
    }
}
