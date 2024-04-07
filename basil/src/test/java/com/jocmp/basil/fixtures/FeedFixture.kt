package com.jocmp.basil.fixtures

import com.jocmp.basil.Feed
import com.jocmp.basil.RandomUUID
import com.jocmp.basil.db.Database
import com.jocmp.basil.persistence.FeedRecords
import com.jocmp.basil.persistence.listMapper
import kotlinx.coroutines.runBlocking
import java.security.SecureRandom
import com.jocmp.basil.db.Feeds as DBFeed

class FeedFixture(private val database: Database) {
    private val records = FeedRecords(database)

    fun create(
        feedID: String = randomID(),
        feedURL: String = "https://example.com"
    ): Feed = runBlocking {
        records.upsert(
            feedID = feedID,
            subscriptionID = randomID(),
            title = "My Feed",
            feedURL = feedURL,
            siteURL = feedURL,
            faviconURL = null,
        )!!
    }

    private fun randomID() = SecureRandom.getInstanceStrong().nextInt().toString()
}
