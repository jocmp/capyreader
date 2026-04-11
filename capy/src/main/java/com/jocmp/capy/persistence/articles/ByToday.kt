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

class ByToday(private val database: Database) {
    fun unreadArticleIDs(
        status: ArticleStatus,
        range: MarkRead,
        sortOrder: SortOrder,
        query: String?,
    ): Query<String> {
        val (_, starred) = status.toStatusPair
        val (afterArticleID, beforeArticleID) = range.toPair

        return database.articlesByStatusQueries.findArticleIDs(
            starred = starred,
            afterArticleID = afterArticleID,
            beforeArticleID = beforeArticleID,
            publishedSince = mapTodayStartDate(),
            newestFirst = isNewestFirst(sortOrder),
            query = query,
        )
    }

    fun pageBoundaries(
        status: ArticleStatus,
        query: String? = null,
        since: OffsetDateTime? = null,
        sortOrder: SortOrder = SortOrder.NEWEST_FIRST,
    ): (anchor: Long?, limit: Long) -> Query<Long> {
        val (read, starred) = status.toStatusPair
        val queries = database.articlesByStatusQueries
        val boundaryQuery = if (isNewestFirst(sortOrder))
            queries::pageBoundaries
        else
            queries::pageBoundariesOldestFirst

        return { anchor, limit ->
            boundaryQuery(
                limit,
                anchor ?: 0L,
                read,
                mapLastRead(read, since),
                starred,
                mapLastUnstarred(starred, since),
                mapTodayStartDate(),
                query,
            )
        }
    }

    fun keyed(
        status: ArticleStatus,
        query: String? = null,
        sortOrder: SortOrder,
        since: OffsetDateTime? = null,
    ): (beginInclusive: Long, endExclusive: Long?) -> Query<Article> {
        val (read, starred) = status.toStatusPair
        val queries = database.articlesByStatusQueries

        return if (isNewestFirst(sortOrder)) {
            { begin, end ->
                queries.keyedNewestFirst(
                    read = read,
                    starred = starred,
                    lastReadAt = mapLastRead(read, since),
                    lastUnstarredAt = mapLastUnstarred(starred, since),
                    publishedSince = mapTodayStartDate(),
                    query = query,
                    beginInclusive = begin,
                    endExclusive = end,
                    mapper = ::listMapper,
                )
            }
        } else {
            { begin, end ->
                queries.keyedOldestFirst(
                    read = read,
                    starred = starred,
                    lastReadAt = mapLastRead(read, since),
                    lastUnstarredAt = mapLastUnstarred(starred, since),
                    publishedSince = mapTodayStartDate(),
                    query = query,
                    beginInclusive = begin,
                    endExclusive = end,
                    mapper = ::listMapper,
                )
            }
        }
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
            lastReadAt = mapLastRead(read, since),
            lastUnstarredAt = mapLastUnstarred(starred, since),
            publishedSince = mapTodayStartDate()
        )
    }

    private fun mapTodayStartDate(): Long {
        return OffsetDateTime.now().minusHours(24).toEpochSecond()
    }
}
