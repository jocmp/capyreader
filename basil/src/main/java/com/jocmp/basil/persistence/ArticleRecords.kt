package com.jocmp.basil.persistence

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.QueryResult
import com.jocmp.basil.Article
import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.db.Database
import java.time.ZonedDateTime

class ArticleRecords internal constructor(
    internal val database: Database
) {
    val byStatus = ByStatus(database)
    val byFeed = ByFeed(database)

    fun fetch(articleID: String): Article? {
        val id = articleID.toLongOrNull()

        id ?: return null

        return database.articlesQueries.findBy(
            articleID = id,
            mapper = ::articleMapper
        ).executeAsOneOrNull()
    }

    fun markRead(articleID: String, lastReadAt: ZonedDateTime = ZonedDateTime.now()) {
        database.articlesQueries.markRead(
            articleID = articleID.toLong(),
            read = true,
            lastReadAt = lastReadAt.toEpochSecond()
        )
    }

    fun markUnread(articleID: String) {
        database.articlesQueries.markRead(
            articleID = articleID.toLong(),
            read = false,
            lastReadAt = null
        )
    }

    fun addStar(articleID: String) {
        database.articlesQueries.markStarred(
            articleID = articleID.toLong(),
            starred = true
        )
    }

    fun removeStar(articleID: String) {
        database.articlesQueries.markStarred(
            articleID = articleID.toLong(),
            starred = false
        )
    }

    fun countUnread(): Map<String, Long> {
        return database.articlesQueries.countUnread().execute {
            val result = mutableMapOf<String, Long>()
            while (it.next().value) {
                val feedID = it.getLong(0)!!.toString()
                val unreadCount = it.getLong(1) ?: 0

                result[feedID] = unreadCount
            }

            QueryResult.Value(result)
        }.value
    }

    class ByFeed(private val database: Database) {
        fun all(
            feedIDs: List<Long>,
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
                mapper = ::articleMapper
            )
        }

        fun count(
            feedIDs: List<Long>,
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
                mapper = ::articleMapper
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
