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
    fun all(
        status: ArticleStatus,
        query: String? = null,
        limit: Long,
        offset: Long,
        sortOrder: SortOrder,
        since: OffsetDateTime?,
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
                publishedSince = mapTodayStartDate(),
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
                publishedSince = mapTodayStartDate(),
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
            publishedSince = mapTodayStartDate(),
            newestFirst = isNewestFirst(sortOrder),
            query = query,
        )
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

    fun neighbors(
        status: ArticleStatus,
        sortOrder: SortOrder,
        since: OffsetDateTime?,
        articleID: String,
    ): Pair<String?, String?> {
        val (read, starred) = status.toStatusPair
        val newestFirst = isNewestFirst(sortOrder)
        val queries = database.articlesByStatusQueries
        val publishedSince = mapTodayStartDate()

        val previous = queries.articleBefore(
            articleID = articleID,
            read = read,
            lastReadAt = mapLastRead(read, since),
            starred = starred,
            lastUnstarredAt = mapLastUnstarred(starred, since),
            publishedSince = publishedSince,
            newestFirst = newestFirst,
        ).executeAsOneOrNull()

        val next = queries.articleAfter(
            articleID = articleID,
            read = read,
            lastReadAt = mapLastRead(read, since),
            starred = starred,
            lastUnstarredAt = mapLastUnstarred(starred, since),
            publishedSince = publishedSince,
            newestFirst = newestFirst,
        ).executeAsOneOrNull()

        return previous to next
    }

    private fun mapTodayStartDate(): Long {
        return OffsetDateTime.now().minusHours(24).toEpochSecond()
    }
}
