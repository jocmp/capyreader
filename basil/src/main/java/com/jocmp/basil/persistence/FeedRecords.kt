package com.jocmp.basil.persistence

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneNotNull
import com.jocmp.basil.ArticleStatus
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basil.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlin.coroutines.coroutineContext

internal class FeedRecords(val database: Database) {
    suspend fun findBy(id: String): Feed? {
        return database.feedsQueries.findBy(id, mapper = ::feedMapper)
            .asFlow()
            .mapToOneNotNull(coroutineContext)
            .firstOrNull()
    }

    suspend fun findFolder(title: String): Folder? {
        val feeds = database.feedsQueries.findByFolder(title, mapper = ::feedMapper)
            .asFlow()
            .mapToList(coroutineContext)
            .first()

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
        folderName: String? = "",
        articleCount: Long = 0
    ): Feed {
        return Feed(
            id = id,
            subscriptionID = subscriptionID,
            name = title,
            feedURL = feedURL,
            siteURL = siteURL ?: "",
            folderName = folderName ?: "",
            count = articleCount
        )
    }
}

private const val TOP_LEVEL_KEY = "top-level"

data class AllFeeds(
    val topLevelFeeds: List<Feed> = emptyList(),
    val folders: List<Folder> = emptyList(),
)
