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

class ByFeed(private val database: Database) {
    fun all(
        feedIDs: List<String>,
        status: ArticleStatus,
        query: String? = null,
        since: OffsetDateTime,
        limit: Long,
        unreadSort: UnreadSortOrder,
        offset: Long,
    ): Query<Article> {
        val (read, starred) = status.toStatusPair

        return database.articlesByFeedQueries.all(
            feedIDs = feedIDs,
            query = query,
            read = read,
            starred = starred,
            limit = limit,
            offset = offset,
            lastReadAt = mapLastRead(read, since),
            newestFirst = isDescendingOrder(status, unreadSort),
            mapper = ::listMapper
        )
    }

    fun unreadArticleIDs(
        status: ArticleStatus,
        feedIDs: List<String>,
        range: MarkRead,
    ): Query<String> {
        val (_, starred) = status.toStatusPair

        val (afterArticleID, beforeArticleID) = range.toPair

        return database.articlesByFeedQueries.findArticleIDs(
            feedIDs = feedIDs,
            starred = starred,
            afterArticleID = afterArticleID,
            beforeArticleID = beforeArticleID,
        )
    }

    fun count(
        feedIDs: List<String>,
        status: ArticleStatus,
        query: String?,
        since: OffsetDateTime
    ): Query<Long> {
        val (read, starred) = status.toStatusPair

        return database.articlesByFeedQueries.countAll(
            feedIDs = feedIDs,
            query = query,
            read = read,
            starred = starred,
            lastReadAt = mapLastRead(read, since)
        )
    }

    fun findIndex(
        articleID: String,
        feedIDs: List<String>,
        status: ArticleStatus,
        query: String?,
        unreadSort: UnreadSortOrder,
        since: OffsetDateTime
    ): Long {
        val (read, starred) = status.toStatusPair
        val newestFirst = status != ArticleStatus.UNREAD ||
                unreadSort == UnreadSortOrder.NEWEST_FIRST

        return database.articlesByFeedQueries
            .findIndex(
                articleID = articleID,
                feedIDs = feedIDs,
                read = read,
                starred = starred,
                lastReadAt = mapLastRead(read, since),
                query = query,
                newestFirst = newestFirst,
            )
            .executeAsOneOrNull() ?: -1
    }
}
