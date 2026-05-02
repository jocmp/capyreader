package com.jocmp.capy.persistence

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jocmp.capy.Feed
import com.jocmp.capy.FeedPriority
import com.jocmp.capy.Folder
import com.jocmp.capy.Velocity
import com.jocmp.capy.common.withIOContext
import com.jocmp.capy.db.Database
import com.jocmp.rssparser.model.ConditionalGetInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

internal class FeedRecords(private val database: Database) {
    suspend fun find(id: String): Feed? = withIOContext {
        database.feedsQueries.find(id, mapper = ::feedMapper).executeAsOneOrNull()
    }

    suspend fun findByFeedURL(feedURL: String): Feed? = withIOContext {
        database.feedsQueries.findByFeedURL(feedURL, mapper = ::feedMapper).executeAsOneOrNull()
    }

    suspend fun findConditionalGet(feedID: String): ConditionalGetInfo = withIOContext {
        val feed = database.feedsQueries.findConditionalGet(feedID).executeAsOneOrNull()

        ConditionalGetInfo(
            etag = feed?.etag,
            lastModified = feed?.last_modified,
        )
    }

    /**
     * Persists conditional-GET state after a successful refresh (200 or 304)
     *
     * - [ConditionalGetInfo.lastModified] is the server's HTTP `Last-Modified` header,
     *   an opaque value for use in the next request as `If-Modified-Since`
     * - [refreshedAt] reflects when the feed was last fetched from the client regardless
     *   of whether the feed had changed. Used locally to compare
     *   staleness of the stored etag/last-modified pair.
     */
    suspend fun updateConditionalGet(
        feedID: String,
        conditionalGet: ConditionalGetInfo,
        refreshedAt: Long,
    ) = withIOContext {
        database.feedsQueries.updateConditionalGet(
            etag = conditionalGet.etag,
            last_modified = conditionalGet.lastModified,
            refreshed_at = refreshedAt,
            feedID = feedID,
        )
    }

    suspend fun upsert(
        feedID: String,
        subscriptionID: String,
        title: String,
        feedURL: String,
        siteURL: String?,
        faviconURL: String?,
        priority: String? = null,
        itunesImageURL: String? = null,
        readLater: Boolean = false,
    ): Feed? = withIOContext {
        database.feedsQueries.upsert(
            id = feedID,
            subscription_id = subscriptionID,
            title = title,
            feed_url = feedURL,
            site_url = siteURL,
            favicon_url = faviconURL,
            priority = priority,
            itunes_image_url = itunesImageURL,
            read_later = readLater,
        )

        find(feedID)
    }

    fun update(feedID: String, title: String) {
        database.feedsQueries.update(
            feedID = feedID,
            title = title,
        )
    }

    fun clearFavicon(feedID: String) {
        database.feedsQueries.updateFavicon(faviconURL = null, feedID = feedID)
    }

    fun updateFavicon(feedID: String, faviconURL: String) {
        database.feedsQueries.updateFavicon(faviconURL = faviconURL, feedID = feedID)
    }

    suspend fun isFullContentEnabled(feedID: String): Boolean = withIOContext {
        database.feedsQueries.isFullContentEnabled(feedID).executeAsOneOrNull() ?: false
    }

    suspend fun updateStickyFullContent(feedID: String, enabled: Boolean) = withIOContext {
        database.feedsQueries.updateStickyFullContent(
            enabled = enabled,
            feedID = feedID
        )
    }

    suspend fun updateOpenInBrowser(feedID: String, enabled: Boolean) = withIOContext {
        database.feedsQueries.updateOpenInBrowser(
            enabled = enabled,
            feedID = feedID
        )
    }

    suspend fun updateVelocity(feedID: String, velocity: Velocity) = withIOContext {
        database.feedsQueries.updateVelocity(
            velocityHours = velocity.hours,
            feedID = feedID,
        )
    }

    suspend fun migrateVelocityForAll(velocityHours: Long?) = withIOContext {
        database.feedsQueries.migrateVelocityForAll(velocityHours = velocityHours)
    }

    suspend fun enableNotifications(feedID: String, enabled: Boolean) = withIOContext {
        database.feedsQueries.enableNotifications(
            enabled = enabled,
            feedID = feedID
        )
    }

    suspend fun toggleAllNotifications(enabled: Boolean) = withIOContext {
        database.feedsQueries.toggleAllNotifications(enabled = enabled)
    }

    suspend fun updateShowUnreadBadge(feedID: String, enabled: Boolean) = withIOContext {
        database.feedsQueries.updateShowUnreadBadge(
            enabled = enabled,
            feedID = feedID
        )
    }

    suspend fun toggleAllShowUnreadBadge(enabled: Boolean) = withIOContext {
        database.feedsQueries.toggleAllShowUnreadBadge(enabled = enabled)
    }

    suspend fun clearStickyFullContent() = withIOContext {
        database.feedsQueries.clearStickyFullContent()
    }

    suspend fun findFolder(title: String): Folder? {
        val feeds = database.feedsQueries.findByFolder(title, mapper = ::feedMapper)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .firstOrNull()
            .orEmpty()

        if (feeds.isEmpty()) {
            return null
        }

        return Folder(title = title, feeds = feeds)
    }

    internal fun taggedFeeds(): Flow<List<Feed>> {
        return database.feedsQueries
            .tagged(mapper = ::feedMapper)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    internal fun feeds(): Flow<List<Feed>> {
        return database.feedsQueries
            .all(mapper = ::feedMapper)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    internal fun removeFeed(feedID: String) {
        database.feedsQueries.delete(listOf(feedID))
    }

    private fun feedMapper(
        id: String,
        subscriptionID: String,
        title: String,
        feedURL: String,
        siteURL: String?,
        faviconURL: String?,
        enableStickyFullContent: Boolean = false,
        enableNotifications: Boolean = false,
        openArticlesInBrowser: Boolean = false,
        priority: String? = null,
        showUnreadBadge: Boolean = true,
        itunesImageURL: String? = null,
        readLater: Boolean = false,
        etag: String? = null,
        lastModified: String? = null,
        conditionalGetRefreshedAt: Long? = null,
        velocityHours: Long? = null,
        folderName: String? = "",
        expanded: Boolean? = false,
    ) = Feed(
        id = id,
        subscriptionID = subscriptionID,
        title = title,
        feedURL = feedURL,
        siteURL = siteURL.orEmpty(),
        faviconURL = faviconURL,
        itunesImageURL = itunesImageURL,
        folderName = folderName.orEmpty(),
        count = 0,
        enableStickyFullContent = enableStickyFullContent,
        enableNotifications = enableNotifications,
        openArticlesInBrowser = openArticlesInBrowser,
        folderExpanded = expanded ?: false,
        priority = FeedPriority.parse(priority),
        showUnreadBadge = showUnreadBadge,
        isReadLater = readLater,
        velocity = Velocity.fromHours(velocityHours),
    )
}
