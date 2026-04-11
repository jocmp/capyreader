package com.jocmp.capy.persistence.articles

import app.cash.sqldelight.Query
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.FeedPriority
import com.jocmp.capy.MarkRead
import com.jocmp.capy.articles.SortOrder
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
        sortOrder: SortOrder,
        offset: Long,
        priority: FeedPriority,
    ): Query<Article> {
        val (read, starred) = status.toStatusPair
        val queries = database.articlesByFeedQueries

        return if (isDescendingOrder(sortOrder)) {
            queries.allNewestFirst(
                feedIDs = feedIDs,
                query = query,
                read = read,
                starred = starred,
                limit = limit,
                offset = offset,
                lastReadAt = mapLastRead(read, since),
                lastUnstarredAt = mapLastUnstarred(starred, since),
                publishedSince = null,
                priorities = priority.inclusivePriorities,
                mapper = ::listMapper
            )
        } else {
            queries.allOldestFirst(
                feedIDs = feedIDs,
                query = query,
                read = read,
                starred = starred,
                limit = limit,
                offset = offset,
                lastReadAt = mapLastRead(read, since),
                lastUnstarredAt = mapLastUnstarred(starred, since),
                publishedSince = null,
                priorities = priority.inclusivePriorities,
                mapper = ::listMapper
            )
        }
    }

    fun unreadArticleIDs(
        status: ArticleStatus,
        feedIDs: List<String>,
        range: MarkRead,
        sortOrder: SortOrder,
        priority: FeedPriority,
        query: String?,
    ): Query<String> {
        val (_, starred) = status.toStatusPair
        val (afterArticleID, beforeArticleID) = range.toPair

        return database.articlesByFeedQueries.findArticleIDs(
            feedIDs = feedIDs,
            starred = starred,
            afterArticleID = afterArticleID,
            beforeArticleID = beforeArticleID,
            publishedSince = null,
            newestFirst = isNewestFirst(sortOrder),
            query = query,
            priorities = priority.inclusivePriorities,
        )
    }

    fun pageBoundaries(
        feedIDs: List<String>,
        status: ArticleStatus,
        query: String? = null,
        since: OffsetDateTime? = null,
        priority: FeedPriority,
        sortOrder: SortOrder = SortOrder.NEWEST_FIRST,
    ): (anchor: Long?, limit: Long) -> Query<Long> {
        val (read, starred) = status.toStatusPair
        val queries = database.articlesByFeedQueries
        val boundaryQuery = if (isDescendingOrder(sortOrder))
            queries::pageBoundaries
        else
            queries::pageBoundariesOldestFirst

        return { anchor, limit ->
            boundaryQuery(
                limit,
                anchor ?: 0L,
                feedIDs,
                read,
                mapLastRead(read, since),
                starred,
                mapLastUnstarred(starred, since),
                null,
                query,
                priority.inclusivePriorities,
            )
        }
    }

    fun keyed(
        feedIDs: List<String>,
        status: ArticleStatus,
        query: String? = null,
        sortOrder: SortOrder,
        since: OffsetDateTime? = null,
        priority: FeedPriority,
    ): (beginInclusive: Long, endExclusive: Long?) -> Query<Article> {
        val (read, starred) = status.toStatusPair
        val queries = database.articlesByFeedQueries

        return if (isDescendingOrder(sortOrder)) {
            { begin, end ->
                queries.keyedNewestFirst(
                    feedIDs = feedIDs,
                    read = read,
                    starred = starred,
                    lastReadAt = mapLastRead(read, since),
                    lastUnstarredAt = mapLastUnstarred(starred, since),
                    publishedSince = null,
                    query = query,
                    priorities = priority.inclusivePriorities,
                    beginInclusive = begin,
                    endExclusive = end,
                    mapper = ::listMapper,
                )
            }
        } else {
            { begin, end ->
                queries.keyedOldestFirst(
                    feedIDs = feedIDs,
                    read = read,
                    starred = starred,
                    lastReadAt = mapLastRead(read, since),
                    lastUnstarredAt = mapLastUnstarred(starred, since),
                    publishedSince = null,
                    query = query,
                    priorities = priority.inclusivePriorities,
                    beginInclusive = begin,
                    endExclusive = end,
                    mapper = ::listMapper,
                )
            }
        }
    }

    fun count(
        feedIDs: List<String>,
        status: ArticleStatus,
        query: String?,
        since: OffsetDateTime?,
        priority: FeedPriority,
    ): Query<Long> {
        val (read, starred) = status.toStatusPair

        return database.articlesByFeedQueries.countAll(
            feedIDs = feedIDs,
            query = query,
            read = read,
            starred = starred,
            lastReadAt = mapLastRead(read, since),
            lastUnstarredAt = mapLastUnstarred(starred, since),
            priorities = priority.inclusivePriorities,
            publishedSince = null
        )
    }
}
