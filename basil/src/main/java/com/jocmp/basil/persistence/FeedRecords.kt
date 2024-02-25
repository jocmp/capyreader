package com.jocmp.basil.persistence

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import com.jocmp.basil.db.Database
import com.jocmp.basil.db.Tagged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

internal class FeedRecords(val database: Database) {
//    internal fun findOrCreate(id: String): Feeds {
//        val existingFeed = database
//            .feedsQueries
//            .findByURL(feed_url = feedURL)
//            .executeAsOneOrNull()
//
//        if (existingFeed != null) {
//            return existingFeed
//        }
//
//        return database.feedsQueries.create(
//            id = "DELETEME",
//            feed_url = feedURL
//        ).executeAsOne()
//    }

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
        folderName: String?,
    ): Feed {
        return Feed(
            id = id,
            subscriptionID = subscriptionID,
            name = title,
            feedURL = feedURL,
            siteURL = siteURL ?: "",
            folderName = folderName ?: "",
        )
    }
}

private const val TOP_LEVEL_KEY = "top-level"

data class AllFeeds(
    val topLevelFeeds: List<Feed> = emptyList(),
    val folders: List<Folder> = emptyList(),
)
