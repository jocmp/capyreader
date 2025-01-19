package com.jocmp.capy.persistence

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleNotification
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.MarkRead
import com.jocmp.capy.articles.UnreadSortOrder
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.common.toDateTimeFromSeconds
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import java.time.ZonedDateTime

internal class ArticleRecords internal constructor(
    private val database: Database
) {
    val byStatus = ByStatus(database)
    val byFeed = ByFeed(database)
    val bySavedSearch = BySavedSearch(database)

    fun find(articleID: String): Article? {
        return database.articlesQueries.findBy(
            articleID = articleID,
            mapper = ::articleMapper
        ).executeAsOneOrNull()
    }

    /**
     * Creates a new status record. On conflict it does nothing.
     */
    fun createStatus(articleID: String, updatedAt: ZonedDateTime, read: Boolean) {
        val updatedAtSeconds = updatedAt.toEpochSecond()

        database.articlesQueries.createStatus(
            article_id = articleID,
            updated_at = updatedAtSeconds,
            read = read,
        )
    }

    /**
     * Create placeholder statuses to be updated
     * by the [findMissingArticles] query
     */
    fun createStatuses(articleIDs: List<String>, updatedAt: ZonedDateTime = nowUTC()) {
        database.transactionWithErrorHandling {
            articleIDs.forEach { createStatus(articleID = it, updatedAt = updatedAt, read = false) }
        }
    }

    /**
     * Upserts a record status. On conflict it overwrites "read" metadata.
     */
    fun updateStatus(articleID: String, updatedAt: ZonedDateTime, read: Boolean, starred: Boolean) {
        val updatedAtSeconds = updatedAt.toEpochSecond()

        val lastReadAt = if (read) {
            updatedAtSeconds
        } else {
            null
        }

        database.articlesQueries.updateStatus(
            article_id = articleID,
            updated_at = updatedAtSeconds,
            last_read_at = lastReadAt,
            read = read,
            starred = starred
        )
    }

    fun findMissingArticles(): List<String> {
        return database
            .articlesQueries
            .findMissingArticles()
            .executeAsList()
    }

    internal suspend fun createNotifications(since: ZonedDateTime): List<ArticleNotification> {
        val articleIDs =
            notificationQueries.articlesToNotify(since = since.toEpochSecond()).executeAsList()

        articleIDs.forEach {
            database.transactionWithErrorHandling {
                notificationQueries.createNotification(article_id = it)
            }
        }

        return allNotifications()
    }

    private suspend fun allNotifications(): List<ArticleNotification> {
        return notificationQueries
            .allNotifications(mapper = ::articleNotificationMapper)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .firstOrNull()
            .orEmpty()
    }

    internal fun countNotifications(): Long {
        return notificationQueries
            .count()
            .executeAsOneOrNull() ?: 0
    }

    internal fun deleteNotification(ids: List<String>) {
        notificationQueries.deleteNotifications(ids = ids)
    }

    fun deleteAllArticles() {
        database.articlesQueries.deleteAllArticles()
    }

    fun deleteOldArticles(before: ZonedDateTime) {
        database.transactionWithErrorHandling {
            val maxDate = before.toEpochSecond()

            database.articlesQueries.deleteArticles(publishedBefore = maxDate)
        }
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
        )
    }

    fun markUnread(articleID: String) {
        database.articlesQueries.markRead(
            articleIDs = listOf(articleID),
            read = false,
            lastReadAt = null,
        )
    }

    fun addStar(articleID: String) {
        database.articlesQueries.markStarred(
            articleID = articleID,
            starred = true,
        )
    }

    fun removeStar(articleID: String) {
        database.articlesQueries.markStarred(
            articleID = articleID,
            starred = false,
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
    fun maxArrivedAt(): ZonedDateTime {
        val max = byStatus.maxArrivedAt()

        max ?: return cutoffDate()

        return max.toDateTimeFromSeconds
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
                byFeed.unreadArticleIDs(
                    filter.status,
                    feedIDs = folderFeedIDs(filter),
                    range = range
                )
            }

            is ArticleFilter.SavedSearches -> bySavedSearch.unreadArticleIDs(
                filter.status,
                savedSearchID = filter.savedSearchID,
                range = range
            )
        }

        return ids.executeAsList()
    }

    private fun folderFeedIDs(filter: ArticleFilter.Folders): List<String> {
        return database
            .taggingsQueries
            .findFeedIDs(folderTitle = filter.folderTitle)
            .executeAsList()
    }

    class ByFeed(private val database: Database) {
        fun all(
            feedIDs: List<String>,
            status: ArticleStatus,
            query: String? = null,
            since: OffsetDateTime,
            limit: Long,
            unreadSort: UnreadSortOrder,
            offset: Long,
        ): Query<Article> {
            val (read, starred) = status.toStatusPair

            return database.articlesQueries.allByFeeds(
                feedIDs = feedIDs,
                query = query,
                read = read,
                starred = starred,
                limit = limit,
                offset = offset,
                lastReadAt = mapLastRead(read, since),
                newestFirst = isDescendingOrder(status, unreadSort),
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
                beforeArticleID = beforeArticleID,
            )
        }

        fun count(
            feedIDs: List<String>,
            status: ArticleStatus,
            query: String?,
            since: OffsetDateTime
        ): Query<Long> {
            val (read, starred) = status.toStatusPair

            return database.articlesQueries.countAllByFeeds(
                feedIDs = feedIDs,
                query = query,
                read = read,
                starred = starred,
                lastReadAt = mapLastRead(read, since)
            )
        }
    }

    class ByStatus(private val database: Database) {
        fun all(
            status: ArticleStatus,
            query: String? = null,
            limit: Long,
            offset: Long,
            unreadSort: UnreadSortOrder,
            since: OffsetDateTime? = null
        ): Query<Article> {
            val (read, starred) = status.toStatusPair
            val newestFirst = status != ArticleStatus.UNREAD ||
                    unreadSort == UnreadSortOrder.NEWEST_FIRST

            return database.articlesQueries.allByStatus(
                read = read,
                starred = starred,
                limit = limit,
                offset = offset,
                lastReadAt = mapLastRead(read, since),
                query = query,
                newestFirst = newestFirst,
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

        fun maxArrivedAt(): Long? {
            return database.articlesQueries.lastUpdatedAt().executeAsOne().MAX
        }

        fun count(
            status: ArticleStatus,
            query: String? = null,
            since: OffsetDateTime? = null
        ): Query<Long> {
            val (read, starred) = status.toStatusPair

            return database.articlesQueries.countAllByStatus(
                read = read,
                starred = starred,
                query = query,
                lastReadAt = mapLastRead(read, since)
            )
        }
    }


    class BySavedSearch(private val database: Database) {
        fun all(
            savedSearchID: String,
            status: ArticleStatus,
            query: String? = null,
            since: OffsetDateTime,
            limit: Long,
            unreadSort: UnreadSortOrder,
            offset: Long,
        ): Query<Article> {
            val (read, starred) = status.toStatusPair

            return database.articlesQueries.allBySavedSearch(
                savedSearchID = savedSearchID,
                query = query,
                read = read,
                starred = starred,
                limit = limit,
                offset = offset,
                lastReadAt = mapLastRead(read, since),
                newestFirst = isDescendingOrder(status, unreadSort),
                mapper = ::listMapper
            )
        }

        fun unreadArticleIDs(
            status: ArticleStatus,
            savedSearchID: String,
            range: MarkRead,
        ): Query<String> {
            val (_, starred) = status.toStatusPair

            val (afterArticleID, beforeArticleID) = range.toPair

            return database.articlesQueries.findArticleIDsBySavedSearch(
                savedSearchID = savedSearchID,
                starred = starred,
                afterArticleID = afterArticleID,
                beforeArticleID = beforeArticleID,
            )
        }

        fun count(
            savedSearchID: String,
            status: ArticleStatus,
            query: String?,
            since: OffsetDateTime
        ): Query<Long> {
            val (read, starred) = status.toStatusPair

            return database.articlesQueries.countAllBySavedSearch(
                savedSearchID = savedSearchID,
                query = query,
                read = read,
                starred = starred,
                lastReadAt = mapLastRead(read, since)
            )
        }
    }

    private fun cutoffDate(): ZonedDateTime {
        return nowUTC().minusMonths(3)
    }

    private val notificationQueries
        get() = database.article_notificationsQueries
}


private fun isDescendingOrder(status: ArticleStatus, unreadSort: UnreadSortOrder) =
    status != ArticleStatus.UNREAD ||
            unreadSort == UnreadSortOrder.NEWEST_FIRST

private fun mapLastRead(read: Boolean?, value: OffsetDateTime?): Long? {
    if (read != null) {
        return value?.toEpochSecond()
    }

    return null
}
