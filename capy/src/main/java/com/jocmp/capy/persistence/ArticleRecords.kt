package com.jocmp.capy.persistence

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleNotification
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.FeedPriority
import com.jocmp.capy.MarkRead
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.common.toDateTimeFromSeconds
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.common.withIOContext
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.articles.ByArticleStatus
import com.jocmp.capy.persistence.articles.ByFeed
import com.jocmp.capy.persistence.articles.BySavedSearch
import com.jocmp.capy.persistence.articles.ByToday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.ZonedDateTime

internal class ArticleRecords internal constructor(
    private val database: Database
) {
    val byStatus = ByArticleStatus(database)
    val byFeed = ByFeed(database)
    val bySavedSearch = BySavedSearch(database)
    val byToday = ByToday(database)

    suspend fun find(articleID: String): Article? = withIOContext {
        database.articlesQueries.findBy(
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

        return notifications(articleIDs)
    }

    private suspend fun notifications(articleIDs: List<String>): List<ArticleNotification> {
        return notificationQueries
            .notificationsByID(
                article_ids = articleIDs,
                mapper = ::articleNotificationMapper
            )
            .asFlow()
            .mapToList(Dispatchers.IO)
            .firstOrNull()
            .orEmpty()
    }

    internal suspend fun countActiveNotifications(): Long = withIOContext {
        notificationQueries
            .countActive()
            .executeAsOneOrNull() ?: 0
    }

    internal suspend fun dismissStaleNotifications() = withIOContext {
        notificationQueries.dismissStaleNotifications(deleted_at = nowUTC().toEpochSecond())
    }

    internal suspend fun dismissNotifications(ids: List<String>) = withIOContext {
        ids.chunked(500).forEach { batchIDs ->
            notificationQueries.dismissNotifications(
                articleIDs = batchIDs,
                deleted_at = nowUTC().toEpochSecond()
            )
        }
    }

    suspend fun deleteAllArticles() = withIOContext {
        database.articlesQueries.deleteAllArticles()
    }

    fun deleteOldArticles(before: ZonedDateTime) {
        database.transactionWithErrorHandling {
            val maxDate = before.toEpochSecond()

            database.articlesQueries.deleteArticles(publishedBefore = maxDate)
        }
    }

    fun deleteOrphanedStatuses(now: ZonedDateTime = nowUTC()) {
        database.transactionWithErrorHandling {
            val cutoffDate = now.minusDays(180).toEpochSecond()

            database.articlesQueries.deleteOrphanedStatuses(cutoffDate = cutoffDate)
        }
    }

    fun markAllUnread(articleIDs: List<String>, updatedAt: ZonedDateTime = nowUTC()) {
        val updated = updatedAt.toEpochSecond()

        database.transactionWithErrorHandling {
            articleIDs.forEach { articleID ->
                database.articlesQueries.upsertUnread(
                    articleID = articleID,
                    updatedAt = updated
                )
            }

            database.articlesQueries.updateStaleUnreads()
        }
    }

    fun markAllStarred(articleIDs: List<String>, updatedAt: ZonedDateTime = nowUTC()) {
        val updated = updatedAt.toEpochSecond()

        database.transactionWithErrorHandling {
            articleIDs.forEach { articleID ->
                database.articlesQueries.upsertStarred(
                    articleID = articleID,
                    updatedAt = updated
                )
            }

            database.articlesQueries.updateStaleStars()
        }
    }

    fun markAllRead(articleIDs: List<String>, lastReadAt: ZonedDateTime = nowUTC()) {
        database.transactionWithErrorHandling {
            val updated = lastReadAt.toEpochSecond()

            database.articlesQueries.markRead(
                articleIDs = articleIDs,
                read = true,
                lastReadAt = updated,
            )
        }
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

    fun countAllBySavedSearch(status: ArticleStatus): Flow<Map<String, Long>> {
        val (read, starred) = status.forCounts

        return database.articlesQueries.countAllBySavedSearch(
            read = read,
            starred = starred,
        )
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.associate { it.saved_search_id to it.COUNT }
            }
    }

    fun countUnread(
        filter: ArticleFilter,
        query: String?,
    ): Flow<Long> {
        val since = null

        val count = when (filter) {
            is ArticleFilter.Articles -> byStatus.count(
                status = filter.articleStatus,
                query = query,
                since = null
            )

            is ArticleFilter.Feeds -> byFeed.count(
                feedIDs = listOf(filter.feedID),
                status = filter.feedStatus,
                query = query,
                since = since,
                priority = FeedPriority.FEED,
            )

            is ArticleFilter.Folders -> byFeed.count(
                feedIDs = folderFeedIDs(filter),
                status = filter.status,
                query = query,
                since = since,
                priority = FeedPriority.CATEGORY,
            )

            is ArticleFilter.SavedSearches -> bySavedSearch.count(
                savedSearchID = filter.savedSearchID,
                status = filter.status,
                query = query,
                since = since,
            )

            is ArticleFilter.Today -> byToday.count(
                status = filter.status,
                query = query,
                since = null
            )
        }

        return count.asFlow().mapToOneOrDefault(0L, Dispatchers.IO)
    }

    suspend fun countToday(status: ArticleStatus): Long = withIOContext {
        byToday.count(
            status = status,
            query = null,
            since = null
        ).executeAsOneOrNull() ?: 0L
    }

    /** Date in UTC */
    fun maxArrivedAt(): ZonedDateTime {
        val max = byStatus.maxArrivedAt()

        max ?: return cutoffDate()

        return max.toDateTimeFromSeconds
    }

    fun filterUnreadStatuses(ids: List<String>): List<String> {
        return database.articlesQueries.filterUnreadStatuses(ids).executeAsList()
    }

    fun unreadArticleIDs(
        filter: ArticleFilter,
        range: MarkRead,
        sortOrder: SortOrder,
        query: String?,
    ): List<String> {
        val ids = when (filter) {
            is ArticleFilter.Articles -> byStatus.unreadArticleIDs(
                filter.articleStatus,
                range = range,
                sortOrder = sortOrder,
                query = query,
            )

            is ArticleFilter.Feeds -> byFeed.unreadArticleIDs(
                filter.feedStatus,
                feedIDs = listOf(filter.feedID),
                range = range,
                sortOrder = sortOrder,
                query = query,
                priority = FeedPriority.FEED,
            )

            is ArticleFilter.Folders -> {
                byFeed.unreadArticleIDs(
                    filter.status,
                    feedIDs = folderFeedIDs(filter),
                    range = range,
                    sortOrder = sortOrder,
                    query = query,
                    priority = FeedPriority.CATEGORY,
                )
            }

            is ArticleFilter.SavedSearches -> bySavedSearch.unreadArticleIDs(
                filter.status,
                savedSearchID = filter.savedSearchID,
                range = range,
                sortOrder = sortOrder,
                query = query,
            )

            is ArticleFilter.Today -> byToday.unreadArticleIDs(
                filter.status,
                range = range,
                sortOrder = sortOrder,
                query = query,
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

    private fun cutoffDate(): ZonedDateTime {
        return nowUTC().minusMonths(3)
    }

    private val notificationQueries
        get() = database.article_notificationsQueries
}
