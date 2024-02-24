package com.jocmp.basil.fixtures

import com.jocmp.basil.RandomUUID
import com.jocmp.basil.db.Database
import com.jocmp.basil.db.Feeds as DBFeed

class FeedFixture(private val database: Database) {
    fun create(
        feedID: String = RandomUUID.generate(),
        feedURL: String = "https://example.com"
    ): DBFeed {
        database.feedsQueries.upsert(
            id = feedID,
            subscription_id = RandomUUID.generate(),
            feed_url = feedURL,
            site_url = feedURL
        )

        return database.feedsQueries.findBy(id = feedID).executeAsOne()
    }
}
