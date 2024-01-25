package com.jocmp.basil.persistence

import com.jocmp.basil.db.Database
import com.jocmp.basil.db.Feeds
import com.jocmp.basil.accounts.ExternalFeed

internal class FeedRecords(val database: Database) {
    internal fun findOrCreate(feedURL: String, externalID: String = feedURL): Feeds {
        val existingFeed = database
            .feedsQueries
            .findByURL(feed_url = feedURL)
            .executeAsOneOrNull()

        if (existingFeed != null) {
            return existingFeed
        }

        return database.feedsQueries.create(
            external_id = feedURL,
            feed_url = feedURL
        ).executeAsOne()
    }

    internal fun removeFeed(feedID: String) {
        database.feedsQueries.delete(feedID = feedID.toLong())
    }
}
