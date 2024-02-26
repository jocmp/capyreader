package com.jocmp.basil.persistence

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.QueryResult
import com.jocmp.basil.Article
import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.common.nowUTC
import com.jocmp.basil.db.Database
import java.time.OffsetDateTime
import java.time.ZonedDateTime

class ArticleRecords internal constructor(
    internal val database: Database
) {
    val byStatus = ByStatus(database)
    val byFeed = ByFeed(database)

    fun fetch(articleID: String): Article? {
        return database.articlesQueries.findBy(
            articleID = articleID,
            mapper = ::articleMapper
        ).executeAsOneOrNull()
    }

    fun markRead(articleID: String, lastReadAt: OffsetDateTime = nowUTC()) {
        val updated = lastReadAt.toEpochSecond()

        database.articlesQueries.markRead(
            articleID = articleID,
            read = true,
            lastReadAt = updated,
            updatedAt = updated
        )
    }

    fun markUnread(articleID: String, updatedAt: OffsetDateTime = nowUTC()) {
        database.articlesQueries.markRead(
            articleID = articleID,
            read = false,
            lastReadAt = null,
            updatedAt = nowUTC().toEpochSecond()
        )
    }

    fun addStar(articleID: String, updatedAt: OffsetDateTime = nowUTC()) {
        database.articlesQueries.markStarred(
            articleID = articleID,
            starred = true,
            updatedAt = updatedAt.toEpochSecond()
        )
    }

    fun removeStar(articleID: String, updatedAt: OffsetDateTime = nowUTC()) {
        database.articlesQueries.markStarred(
            articleID = articleID,
            starred = false,
            updatedAt = updatedAt.toEpochSecond()
        )
    }

    fun countAll(status: ArticleStatus): Map<String, Long> {
//        val (read, starred) = status.forCounts
//
//        return database.articlesQueries.countAll(
//            read = read,
//            starred = starred,
//        ).execute {
//            val result = mutableMapOf<String, Long>()
//            while (it.next().value) {
//                val feedID = it.getLong(0)!!.toString()
//                val unreadCount = it.getLong(1) ?: 0
//
//                result[feedID] = unreadCount
//            }
//
//            QueryResult.Value(result)
//        }.value
        return emptyMap()
    }

    class ByFeed(private val database: Database) {
        fun all(
            feedIDs: List<String>,
            status: ArticleStatus,
            limit: Long,
            offset: Long,
            since: ZonedDateTime
        ): Query<Article> {
            val (read, starred) = status.toStatusPair

            return database.articlesQueries.findByFeeds(
                feedIDs = feedIDs,
                read = read,
                starred = starred,
                limit = limit,
                offset = offset,
                lastReadAt = mapLastRead(read, since),
                mapper = ::listMapper
            )
        }

        fun count(
            feedIDs: List<String>,
            status: ArticleStatus,
            since: ZonedDateTime
        ): Query<Long> {
            val (read, starred) = status.toStatusPair

            return database.articlesQueries.countByFeeds(
                feedIDs = feedIDs,
                read = read,
                starred = starred,
                lastReadAt = mapLastRead(read, since)
            )
        }
    }

    class ByStatus(private val database: Database) {
        fun all(
            status: ArticleStatus,
            limit: Long,
            offset: Long,
            since: ZonedDateTime? = null
        ): Query<Article> {
            val (read, starred) = status.toStatusPair

            return database.articlesQueries.findByStatus(
                read = read,
                starred = starred,
                limit = limit,
                offset = offset,
                lastReadAt = mapLastRead(read, since),
                mapper = ::listMapper
            )
        }

        fun count(status: ArticleStatus, since: ZonedDateTime? = null): Query<Long> {
            val (read, starred) = status.toStatusPair

            return database.articlesQueries.countByStatus(
                read = read,
                starred = starred,
                lastReadAt = mapLastRead(read, since)
            )
        }
    }
}

private fun mapLastRead(read: Boolean?, value: ZonedDateTime?): Long? {
    if (read != null) {
        return value?.toEpochSecond()
    }

    return null
}
