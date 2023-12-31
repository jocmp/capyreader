package com.jocmp.basil.fixtures

import com.jocmp.basil.RandomUUID
import com.jocmp.basil.db.Database
import com.jocmp.basil.db.Feeds as DBFeed

class FeedFixture(private val database: Database) {
    fun create(
        externalID: String = RandomUUID.generate(),
        feedURL: String
    ): DBFeed {
        return database.feedsQueries.create(
            external_id = externalID,
            feed_url = feedURL
        ).executeAsOne()
    }
}
