package com.jocmp.basil.persistence

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jocmp.basil.Article
import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.common.nowUTC
import com.jocmp.basil.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.Objects

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

    fun markRead(articleID: String, lastReadAt: ZonedDateTime = nowUTC()) {
        val updated = lastReadAt.toEpochSecond()

        database.articlesQueries.markRead(
            articleID = articleID,
            read = true,
            lastReadAt = updated,
            updatedAt = updated
        )
    }

    fun markUnread(articleID: String, updatedAt: ZonedDateTime = nowUTC()) {
        database.articlesQueries.markRead(
            articleID = articleID,
            read = false,
            lastReadAt = null,
            updatedAt = nowUTC().toEpochSecond()
        )
    }

    fun addStar(articleID: String, updatedAt: ZonedDateTime = nowUTC()) {
        database.articlesQueries.markStarred(
            articleID = articleID,
            starred = true,
            updatedAt = updatedAt.toEpochSecond()
        )
    }

    fun removeStar(articleID: String, updatedAt: ZonedDateTime = nowUTC()) {
        database.articlesQueries.markStarred(
            articleID = articleID,
            starred = false,
            updatedAt = updatedAt.toEpochSecond()
        )
    }

    fun countAll(status: ArticleStatus): Flow<Map<String, Long>> {
        val (read, starred) = status.forCounts

        return database.articlesQueries.countAll(
            read = read,
            starred = starred,
        )
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.associate {
                    val feedID = it.feed_id ?: ""

                    feedID to it.COUNT
                }
            }
    }

    class ByFeed(private val database: Database) {
        fun all(
            feedIDs: List<String>,
            status: ArticleStatus,
            limit: Long,
            offset: Long,
            since: OffsetDateTime
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
            since: OffsetDateTime
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
            since: OffsetDateTime? = null
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

        fun count(status: ArticleStatus, since: OffsetDateTime? = null): Query<Long> {
            val (read, starred) = status.toStatusPair

            return database.articlesQueries.countByStatus(
                read = read,
                starred = starred,
                lastReadAt = mapLastRead(read, since)
            )
        }
    }
}

private fun mapLastRead(read: Boolean?, value: OffsetDateTime?): Long? {
    if (read != null) {
        return value?.toEpochSecond()
    }

    return null
}
