package com.jocmp.basil.feeds

import com.jocmp.basil.db.Database
import com.jocmp.basil.db.Feeds

internal class FeedRecords(val database: Database) {
    internal fun findOrCreate(externalFeed: ExternalFeed): Feeds {
        val existingFeed = database
            .feedsQueries
            .findByExternalID(external_id = externalFeed.externalID)
            .executeAsOneOrNull()

        if (existingFeed != null) {
            return existingFeed
        }

        return database.feedsQueries.create(
            external_id = externalFeed.externalID,
            feed_url = externalFeed.feedURL
        ).executeAsOne()
    }
}
