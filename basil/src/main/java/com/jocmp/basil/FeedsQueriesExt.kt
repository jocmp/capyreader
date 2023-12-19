package com.jocmp.basil

import com.jocmp.basil.accounts.ExternalFeed
import com.jocmp.basil.db.Database
import com.jocmp.basil.db.Feeds
import com.jocmp.basil.db.FeedsQueries
import java.net.URL

internal val Database.feeds: FeedsQueries
    get() = feedsQueries

internal fun FeedsQueries.findOrCreate(externalFeed: ExternalFeed, feedURL: URL): Feeds {
    val existingFeed = findByExternalID(external_id = externalFeed.externalID).executeAsOneOrNull()

    if (existingFeed != null) {
        return existingFeed
    }

    return create(
        external_id = externalFeed.externalID,
        feed_url = feedURL.toString()
    ).executeAsOne()
}
