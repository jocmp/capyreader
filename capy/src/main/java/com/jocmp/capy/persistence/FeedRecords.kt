package com.jocmp.capy.persistence

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneNotNull
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.coroutines.coroutineContext

internal class FeedRecords(private val database: Database) {
    suspend fun findBy(id: String): Feed? {
        return database.feedsQueries.findBy(id, mapper = ::feedMapper)
            .asFlow()
            .mapToOneNotNull(coroutineContext)
            .firstOrNull()
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

        return findBy(feedID)
    }

    fun update(feedID: String, title: String) {
        database.feedsQueries.update(
            feedID = feedID,
            title = title,
        )
    }

    fun updateStickyFullContent(feedID: String, enabled: Boolean) {
        database.feedsQueries.updateStickyFullContent(
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

     internal fun feeds(): Flow<List<Feed>> {
        return database.feedsQueries
            .tagged(mapper = ::feedMapper)
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
        folderName: String? = "",
        articleCount: Long = 0,
    ) = Feed(
        id = id,
        subscriptionID = subscriptionID,
        title = title,
        feedURL = feedURL,
        siteURL = siteURL.orEmpty(),
        faviconURL = faviconURL,
        folderName = folderName.orEmpty(),
        count = articleCount,
        enableStickyFullContent = enableStickyFullContent,
        enableNotifications = enableNotifications
    )
}
