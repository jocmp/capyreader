package com.jocmp.capy.persistence.articles

import app.cash.sqldelight.Query
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.MarkRead
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.listMapper
import com.jocmp.capy.persistence.toStatusPair
import java.time.OffsetDateTime

class BySavedSearch(private val database: Database) {
    fun all(
        savedSearchID: String,
        status: ArticleStatus,
        query: String? = null,
        since: OffsetDateTime,
        limit: Long,
        sortOrder: SortOrder,
        offset: Long,
    ): Query<Article> {
        val (read, starred) = status.toStatusPair

        val queries = database.articlesBySavedSearchQueries

        return if (isDescendingOrder(sortOrder)) {
            queries.allNewestFirst(
                savedSearchID = savedSearchID,
                query = query,
                read = read,
                starred = starred,
                limit = limit,
                offset = offset,
                lastReadAt = mapLastRead(read, since),
                lastUnstarredAt = mapLastUnstarred(starred, since),
                publishedSince = null,
                mapper = ::listMapper
            )
        } else {
            queries.allOldestFirst(
                savedSearchID = savedSearchID,
                query = query,
                read = read,
                starred = starred,
                limit = limit,
                offset = offset,
                lastReadAt = mapLastRead(read, since),
                lastUnstarredAt = mapLastUnstarred(starred, since),
                publishedSince = null,
                mapper = ::listMapper
            )
        }
    }

    fun keyed(
        savedSearchID: String,
        status: ArticleStatus,
        query: String? = null,
        since: OffsetDateTime,
        sortOrder: SortOrder,
        beginInclusive: Long,
        endExclusive: Long?,
    ): Query<Article> {
        val (read, starred) = status.toStatusPair
        val queries = database.articlesBySavedSearchQueries

        return if (isDescendingOrder(sortOrder)) {
            queries.keyedNewestFirst(
                beginInclusive = beginInclusive,
                endExclusive = endExclusive,
                savedSearchID = savedSearchID,
                query = query,
                read = read,
                starred = starred,
                lastReadAt = mapLastRead(read, since),
                lastUnstarredAt = mapLastUnstarred(starred, since),
                publishedSince = null,
                mapper = ::listMapper
            )
        } else {
            queries.keyedOldestFirst(
                beginInclusive = beginInclusive,
                endExclusive = endExclusive,
                savedSearchID = savedSearchID,
                query = query,
                read = read,
                starred = starred,
                lastReadAt = mapLastRead(read, since),
                lastUnstarredAt = mapLastUnstarred(starred, since),
                publishedSince = null,
                mapper = ::listMapper
            )
        }
    }

    fun pageBoundaries(
        savedSearchID: String,
        status: ArticleStatus,
        query: String? = null,
        since: OffsetDateTime,
        sortOrder: SortOrder,
        anchor: Long?,
        limit: Long,
    ): Query<Long> {
        val (read, starred) = status.toStatusPair
        val queries = database.articlesBySavedSearchQueries

        return if (isDescendingOrder(sortOrder)) {
            queries.pageBoundariesNewestFirst(
                limit = limit,
                anchor = anchor,
                savedSearchID = savedSearchID,
                query = query,
                read = read,
                starred = starred,
                lastReadAt = mapLastRead(read, since),
                lastUnstarredAt = mapLastUnstarred(starred, since),
                publishedSince = null,
            )
        } else {
            queries.pageBoundariesOldestFirst(
                limit = limit,
                anchor = anchor,
                savedSearchID = savedSearchID,
                query = query,
                read = read,
                starred = starred,
                lastReadAt = mapLastRead(read, since),
                lastUnstarredAt = mapLastUnstarred(starred, since),
                publishedSince = null,
            )
        }
    }

    fun unreadArticleIDs(
        status: ArticleStatus,
        savedSearchID: String,
        range: MarkRead,
        sortOrder: SortOrder,
        query: String?,
    ): Query<String> {
        val (_, starred) = status.toStatusPair

        val (afterArticleID, beforeArticleID) = range.toPair

        return database.articlesBySavedSearchQueries.findArticleIDs(
            savedSearchID = savedSearchID,
            starred = starred,
            afterArticleID = afterArticleID,
            beforeArticleID = beforeArticleID,
            publishedSince = null,
            newestFirst = isNewestFirst(sortOrder),
            query = query,
        )
    }

    fun count(
        savedSearchID: String,
        status: ArticleStatus,
        query: String?,
        since: OffsetDateTime?
    ): Query<Long> {
        val (read, starred) = status.toStatusPair

        return database.articlesBySavedSearchQueries.countAll(
            savedSearchID = savedSearchID,
            query = query,
            read = read,
            starred = starred,
            lastReadAt = mapLastRead(read, since),
            lastUnstarredAt = mapLastUnstarred(starred, since),
            publishedSince = null
        )
    }
}
