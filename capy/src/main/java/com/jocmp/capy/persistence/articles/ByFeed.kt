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

    fun neighbors(
        feedIDs: List<String>,
        status: ArticleStatus,
        sortOrder: SortOrder,
        since: OffsetDateTime?,
        priority: FeedPriority,
        articleID: String,
    ): Pair<String?, String?> {
        val (read, starred) = status.toStatusPair
        val newestFirst = isNewestFirst(sortOrder)
        val queries = database.articlesByFeedQueries

        val findBefore =
            if (newestFirst) queries::articleBeforeNewestFirst else queries::articleBeforeOldestFirst
        val findAfter =
            if (newestFirst) queries::articleAfterNewestFirst else queries::articleAfterOldestFirst

        val previous = findBefore(
            articleID,
            feedIDs,
            read,
            mapLastRead(read, since),
            starred,
            mapLastUnstarred(starred, since),
            null,
            priority.inclusivePriorities,
        ).executeAsOneOrNull()

        val next = findAfter(
            articleID,
            feedIDs,
            read,
            mapLastRead(read, since),
            starred,
            mapLastUnstarred(starred, since),
            null,
            priority.inclusivePriorities,
        ).executeAsOneOrNull()

        return previous to next
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
