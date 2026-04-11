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

    fun pageBoundaries(
        savedSearchID: String,
        status: ArticleStatus,
        query: String? = null,
        since: OffsetDateTime? = null,
        sortOrder: SortOrder = SortOrder.NEWEST_FIRST,
    ): (anchor: Long?, limit: Long) -> Query<Long> {
        val (read, starred) = status.toStatusPair
        val queries = database.articlesBySavedSearchQueries
        val boundaryQuery = if (isNewestFirst(sortOrder))
            queries::pageBoundaries
        else
            queries::pageBoundariesOldestFirst

        return { anchor, limit ->
            boundaryQuery(
                limit,
                anchor ?: 0L,
                savedSearchID,
                read,
                mapLastRead(read, since),
                starred,
                mapLastUnstarred(starred, since),
                null,
                query,
            )
        }
    }

    fun keyed(
        savedSearchID: String,
        status: ArticleStatus,
        query: String? = null,
        sortOrder: SortOrder,
        since: OffsetDateTime? = null,
    ): (beginInclusive: Long, endExclusive: Long?) -> Query<Article> {
        val (read, starred) = status.toStatusPair
        val queries = database.articlesBySavedSearchQueries

        return if (isDescendingOrder(sortOrder)) {
            { begin, end ->
                queries.keyedNewestFirst(
                    savedSearchID = savedSearchID,
                    read = read,
                    starred = starred,
                    lastReadAt = mapLastRead(read, since),
                    lastUnstarredAt = mapLastUnstarred(starred, since),
                    publishedSince = null,
                    query = query,
                    beginInclusive = begin,
                    endExclusive = end,
                    mapper = ::listMapper,
                )
            }
        } else {
            { begin, end ->
                queries.keyedOldestFirst(
                    savedSearchID = savedSearchID,
                    read = read,
                    starred = starred,
                    lastReadAt = mapLastRead(read, since),
                    lastUnstarredAt = mapLastUnstarred(starred, since),
                    publishedSince = null,
                    query = query,
                    beginInclusive = begin,
                    endExclusive = end,
                    mapper = ::listMapper,
                )
            }
        }
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
