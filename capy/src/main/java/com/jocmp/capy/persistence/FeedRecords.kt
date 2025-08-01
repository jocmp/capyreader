package com.jocmp.capy.persistence

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.coroutines.coroutineContext

internal class FeedRecords(private val database: Database) {
    fun find(id: String): Feed? {
        return database.feedsQueries.find(id, mapper = ::feedMapper).executeAsOneOrNull()
    }

    suspend fun upsert(
        feedID: String,
        subscriptionID: String,
        title: String,
        feedURL: String,
        siteURL: String?,
        faviconURL: String?
    ): Feed? {
        database.feedsQueries.upsert(
            id = feedID,
            subscription_id = subscriptionID,
            title = title,
            feed_url = feedURL,
            site_url = siteURL,
            favicon_url = faviconURL,
        )

        return find(feedID)
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

    fun isFullContentEnabled(feedID: String): Boolean {
        return database.feedsQueries.isFullContentEnabled(feedID).executeAsOneOrNull() ?: false
    }

    fun updateStickyFullContent(feedID: String, enabled: Boolean) {
        database.feedsQueries.updateStickyFullContent(
            enabled = enabled,
            feedID = feedID
        )
    }

    fun updateOpenInBrowser(feedID: String, enabled: Boolean) {
        database.feedsQueries.updateOpenInBrowser(
            enabled = enabled,
            feedID = feedID
        )
    }

    fun enableNotifications(feedID: String, enabled: Boolean) {
        database.feedsQueries.enableNotifications(
            enabled = enabled,
            feedID = feedID
        )
    }

    fun toggleAllNotifications(enabled: Boolean) {
        database.feedsQueries.toggleAllNotifications(enabled = enabled)
    }

    fun clearStickyFullContent() {
        database.feedsQueries.clearStickyFullContent()
    }

    suspend fun findFolder(title: String): Folder? {
        val feeds = database.feedsQueries.findByFolder(title, mapper = ::feedMapper)
            .asFlow()
            .mapToList(coroutineContext)
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
        folderName: String? = "",
        expanded: Boolean? = false,
    ) = Feed(
        id = id,
        subscriptionID = subscriptionID,
        title = title,
        feedURL = feedURL,
        siteURL = siteURL.orEmpty(),
        faviconURL = faviconURL,
        folderName = folderName.orEmpty(),
        count = 0,
        enableStickyFullContent = enableStickyFullContent,
        enableNotifications = enableNotifications,
        openArticlesInBrowser = openArticlesInBrowser,
        folderExpanded = expanded ?: false
    )
}
