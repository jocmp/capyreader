package com.jocmp.capy.fixtures

import com.jocmp.capy.Feed
import com.jocmp.capy.db.Database
import com.jocmp.capy.persistence.FeedRecords
import com.jocmp.capy.persistence.TaggingRecords
import kotlinx.coroutines.runBlocking
import java.security.SecureRandom

internal class FeedFixture(
    database: Database,
    private val records: FeedRecords = FeedRecords(database),
    private val taggings: TaggingRecords = TaggingRecords(database)
) {
    fun create(
        feedID: String = randomID(),
        feedURL: String = "https://example.com",
        title: String = "My Feed",
        folderNames: List<String> = emptyList(),
        enableNotifications: Boolean = false,
    ): Feed = runBlocking {
        val feed = records.upsert(
            feedID = feedID,
            subscriptionID = randomID(),
            title = title,
            feedURL = feedURL,
            siteURL = feedURL,
            faviconURL = null,
        )!!

        if (enableNotifications) {
            records.enableNotifications(feed.id, enabled = true)
        }

        folderNames.distinct().forEach { name ->
            taggings.upsert("${feed.id}:${name}", name = name, feedID = feed.id)
        }

        records.find(feed.id)!!
    }

    private fun randomID() = SecureRandom.getInstanceStrong().nextInt().toString()
}
