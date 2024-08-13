package com.jocmp.capy.persistence

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.MarkRead
import com.jocmp.capy.common.nowUTC
import com.jocmp.capy.common.toDateTimeFromSeconds
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import java.time.ZonedDateTime

internal class ArticleRecords internal constructor(
    private val database: Database
) {
    val byStatus = ByStatus(database)
    val byFeed = ByFeed(database)

    fun find(articleID: String): Article? {
        return database.articlesQueries.findBy(
            articleID = articleID,
            mapper = ::articleMapper
        ).executeAsOneOrNull()
    }

    fun findMissingArticles(): List<Long> {
        return database
            .articlesQueries
            .findMissingArticles()
            .executeAsList()
            .map { it.toLong() }
    }

    fun deleteOldArticles() {
        val maxDate = cutoffDate().toEpochSecond()

        database.articlesQueries.deleteArticles(publishedBefore = maxDate)
    }

    fun markAllUnread(articleIDs: List<String>, updatedAt: ZonedDateTime = nowUTC()) {
        val updated = updatedAt.toEpochSecond()

        database.transactionWithErrorHandling {
            database.articlesQueries.updateStaleUnreads(excludedIDs = articleIDs)

            articleIDs.forEach { articleID ->
                database.articlesQueries.upsertUnread(
                    articleID = articleID,
                    updatedAt = updated
                )
            }
        }
    }

    fun markAllStarred(articleIDs: List<String>, updatedAt: ZonedDateTime = nowUTC()) {
        val updated = updatedAt.toEpochSecond()

        database.transactionWithErrorHandling {
            database.articlesQueries.updateStaleStars(excludedIDs = articleIDs)

            articleIDs.forEach { articleID ->
                database.articlesQueries.upsertStarred(
                    articleID = articleID,
                    updatedAt = updated
                )
            }
        }
    }

    fun markAllRead(articleIDs: List<String>, lastReadAt: ZonedDateTime = nowUTC()) {
        val updated = lastReadAt.toEpochSecond()

        database.articlesQueries.markRead(
            articleIDs = articleIDs,
            read = true,
            lastReadAt = updated,
            updatedAt = updated
        )
    }

    fun markUnread(articleID: String, updatedAt: ZonedDateTime = nowUTC()) {
        database.articlesQueries.markRead(
            articleIDs = listOf(articleID),
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

    /** Date in UTC */
    fun maxUpdatedAt(): String {
        val max = database.articlesQueries.lastUpdatedAt().executeAsOne().MAX

        max ?: return cutoffDate().toString()

        return max.toDateTimeFromSeconds.toString()
    }

    fun unreadArticleIDs(filter: ArticleFilter, range: MarkRead): List<String> {
        val ids = when (filter) {
            is ArticleFilter.Articles -> byStatus.unreadArticleIDs(
                filter.articleStatus,
                range = range
            )

            is ArticleFilter.Feeds -> byFeed.unreadArticleIDs(
                filter.feedStatus,
                feedIDs = listOf(filter.feedID),
                range = range
            )

            is ArticleFilter.Folders -> {
                val feedIDs = database
                    .taggingsQueries
                    .findFeedIDs(folderTitle = filter.folderTitle)
                    .executeAsList()

                byFeed.unreadArticleIDs(
                    filter.status,
                    feedIDs = feedIDs,
                    range = range
                )
            }
        }

        return ids.executeAsList()
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

        fun unreadArticleIDs(
            status: ArticleStatus,
            feedIDs: List<String>,
            range: MarkRead,
        ): Query<String> {
            val (_, starred) = status.toStatusPair
            val (afterArticleID, beforeArticleID) = range.toPair

            return database.articlesQueries.findArticleIDsByFeeds(
                feedIDs = feedIDs,
                starred = starred,
                afterArticleID = afterArticleID,
                beforeArticleID = beforeArticleID
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

        fun unreadArticleIDs(status: ArticleStatus, range: MarkRead): Query<String> {
            val (_, starred) = status.toStatusPair
            val (afterArticleID, beforeArticleID) = range.toPair


            return database.articlesQueries.findArticleIDsByStatus(
                starred = starred,
                afterArticleID = afterArticleID,
                beforeArticleID = beforeArticleID
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

    internal fun cutoffDate(): ZonedDateTime {
        return nowUTC().minusMonths(3)
    }
}

private fun mapLastRead(read: Boolean?, value: OffsetDateTime?): Long? {
    if (read != null) {
        return value?.toEpochSecond()
    }

    return null
}
