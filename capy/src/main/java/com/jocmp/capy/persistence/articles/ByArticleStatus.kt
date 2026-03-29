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

class ByArticleStatus(private val database: Database) {
    fun all(
        status: ArticleStatus,
        query: String? = null,
        limit: Long,
        offset: Long,
        sortOrder: SortOrder,
        since: OffsetDateTime? = null
    ): Query<Article> {
        val (read, starred) = status.toStatusPair
        val queries = database.articlesByStatusQueries

        return if (isNewestFirst(sortOrder)) {
            queries.allNewestFirst(
                read = read,
                starred = starred,
                limit = limit,
                offset = offset,
                lastReadAt = mapLastRead(read, since),
                lastUnstarredAt = mapLastUnstarred(starred, since),
                publishedSince = null,
                query = query,
                mapper = ::listMapper
            )
        } else {
            queries.allOldestFirst(
                read = read,
                starred = starred,
                limit = limit,
                offset = offset,
                lastReadAt = mapLastRead(read, since),
                lastUnstarredAt = mapLastUnstarred(starred, since),
                publishedSince = null,
                query = query,
                mapper = ::listMapper
            )
        }
    }

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
            publishedSince = null,
            newestFirst = isNewestFirst(sortOrder),
            query = query,
        )
    }

    fun maxArrivedAt(): Long? {
        return database.articlesQueries.lastUpdatedAt().executeAsOne().MAX
    }

    fun pageBoundaries(
        status: ArticleStatus,
        query: String? = null,
        since: OffsetDateTime? = null,
    ): (anchor: Long?, limit: Long) -> Query<Long> {
        val (read, starred) = status.toStatusPair

        return { anchor, limit ->
            database.articlesByStatusQueries.pageBoundaries(
                read = read,
                starred = starred,
                lastReadAt = mapLastRead(read, since),
                lastUnstarredAt = mapLastUnstarred(starred, since),
                publishedSince = null,
                query = query,
                anchor = anchor,
                limit = limit,
                mapper = { publishedAt -> publishedAt ?: 0L }
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
            publishedSince = null
        )
    }

}
