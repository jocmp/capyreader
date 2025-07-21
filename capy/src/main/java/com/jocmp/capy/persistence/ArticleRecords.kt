package com.jocmp.capy.persistence

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleNotification
import com.jocmp.capy.ArticlePages
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.MarkRead
import com.jocmp.capy.articles.UnreadSortOrder
import com.jocmp.capy.common.TimeHelpers.nowUTC
import com.jocmp.capy.common.toDateTimeFromSeconds
import com.jocmp.capy.common.transactionWithErrorHandling
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.articles.ByArticleStatus
import com.jocmp.capy.persistence.articles.ByFeed
import com.jocmp.capy.persistence.articles.BySavedSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import java.time.ZonedDateTime

internal class ArticleRecords internal constructor(
    private val database: Database
) {
    val byStatus = ByArticleStatus(database)
    val byFeed = ByFeed(database)
    val bySavedSearch = BySavedSearch(database)

    fun find(articleID: String): Article? {
        return database.articlesQueries.findBy(
            articleID = articleID,
            mapper = ::articleMapper
        ).executeAsOneOrNull()
    }

    fun findPages(
        articleID: String,
        filter: ArticleFilter,
        query: String?,
        unreadSort: UnreadSortOrder,
        since: OffsetDateTime
    ): Flow<ArticlePages?> {
        return when (filter) {
            is ArticleFilter.Articles -> byStatus.findPages(
                articleID = articleID,
                status = filter.articleStatus,
                query = query,
                unreadSort = unreadSort,
                since = since
            )

            is ArticleFilter.Feeds -> byFeed.findPages(
                articleID = articleID,
                feedIDs = listOf(filter.feedID),
                status = filter.status,
                query = query,
                unreadSort = unreadSort,
                since = since
            )

            is ArticleFilter.Folders -> byFeed.findPages(
                articleID = articleID,
                feedIDs = folderFeedIDs(filter),
                status = filter.status,
                query = query,
                unreadSort = unreadSort,
                since = since
            )

            is ArticleFilter.SavedSearches -> bySavedSearch.findPages(
                articleID = articleID,
                savedSearchID = filter.savedSearchID,
                status = filter.status,
                query = query,
                unreadSort = unreadSort,
                since = since
            )
        }
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
            notificationQueries.createNotification(article_id = it)
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

    internal fun countActiveNotifications(): Long {
        return notificationQueries
            .countActive()
            .executeAsOneOrNull() ?: 0
    }

    internal fun dismissStaleNotifications() {
        notificationQueries.dismissStaleNotifications(deleted_at = nowUTC().toEpochSecond())
    }

    internal fun dismissNotifications(ids: List<String>) {
        ids.chunked(500).forEach { batchIDs ->
            notificationQueries.dismissNotifications(
                articleIDs = batchIDs,
                deleted_at = nowUTC().toEpochSecond()
            )
        }
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
        unreadSort: UnreadSortOrder,
        query: String?,
    ): List<String> {
        val ids = when (filter) {
            is ArticleFilter.Articles -> byStatus.unreadArticleIDs(
                filter.articleStatus,
                range = range,
                unreadSort = unreadSort,
                query = query,
            )

            is ArticleFilter.Feeds -> byFeed.unreadArticleIDs(
                filter.feedStatus,
                feedIDs = listOf(filter.feedID),
                range = range,
                unreadSort = unreadSort,
                query = query,
            )

            is ArticleFilter.Folders -> {
                byFeed.unreadArticleIDs(
                    filter.status,
                    feedIDs = folderFeedIDs(filter),
                    range = range,
                    unreadSort = unreadSort,
                    query = query,
                )
            }

            is ArticleFilter.SavedSearches -> bySavedSearch.unreadArticleIDs(
                filter.status,
                savedSearchID = filter.savedSearchID,
                range = range,
                unreadSort = unreadSort,
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
