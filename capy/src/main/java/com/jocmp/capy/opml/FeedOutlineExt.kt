package com.jocmp.capy.opml

import com.jocmp.capy.Feed
import com.jocmp.capy.db.Feeds as DBFeed
import com.jocmp.capy.opml.Feed as OPMLFeed

internal fun OPMLFeed.asFeed(feeds: Map<Long, DBFeed> = mapOf()): Feed? {
    val parsedID = id?.toLongOrNull() ?: return null
    val feed = feeds[parsedID]

    feed ?: return null

    return Feed(
        id = feed.id,
        subscriptionID = feed.subscription_id,
        title = title ?: "",
        feedURL = xmlUrl ?: "",
    )
}
