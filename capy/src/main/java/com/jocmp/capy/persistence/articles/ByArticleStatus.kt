package com.jocmp.capy.persistence.articles

import app.cash.sqldelight.Query
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.MarkRead
import com.jocmp.capy.articles.UnreadSortOrder
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.listMapper
import com.jocmp.capy.persistence.toStatusPair
import java.time.OffsetDateTime

class ByArticleStatus(private val database: Database) {
    fun all(
        status: ArticleStatus,
        query: String? = null,
        limit: Long,
        offset: Long,
        unreadSort: UnreadSortOrder,
        since: OffsetDateTime? = null
    ): Query<Article> {
        val (read, starred) = status.toStatusPair
        val newestFirst = status != ArticleStatus.UNREAD ||
                unreadSort == UnreadSortOrder.NEWEST_FIRST

        return database.articlesByStatusQueries.all(
            read = read,
            starred = starred,
            limit = limit,
            offset = offset,
            lastReadAt = mapLastRead(read, since),
            query = query,
            newestFirst = newestFirst,
            mapper = ::listMapper
        )
    }

    fun unreadArticleIDs(status: ArticleStatus, range: MarkRead): Query<String> {
        val (_, starred) = status.toStatusPair
        val (afterArticleID, beforeArticleID) = range.toPair

        return database.articlesByStatusQueries.findArticleIDs(
            starred = starred,
            afterArticleID = afterArticleID,
            beforeArticleID = beforeArticleID
        )
    }

    fun maxArrivedAt(): Long? {
        return database.articlesQueries.lastUpdatedAt().executeAsOne().MAX
    }

    fun count(
        status: ArticleStatus,
        query: String? = null,
        since: OffsetDateTime? = null
    ): Query<Long> {
        val (read, starred) = status.toStatusPair

        return database.articlesByStatusQueries.countAll(
            read = read,
            starred = starred,
            query = query,
            lastReadAt = mapLastRead(read, since)
        )
    }

    fun findIndex(
        articleID: String,
        status: ArticleStatus,
        query: String?,
        unreadSort: UnreadSortOrder,
        since: OffsetDateTime
    ): Long {
        val (read, starred) = status.toStatusPair
        val newestFirst = status != ArticleStatus.UNREAD ||
                unreadSort == UnreadSortOrder.NEWEST_FIRST

        return database.articlesByStatusQueries
            .findIndex(
                articleID = articleID,
                read = read,
                starred = starred,
                lastReadAt = mapLastRead(read, since),
                query = query,
                newestFirst = newestFirst,
            )
            .executeAsOneOrNull() ?: -1
    }
}
