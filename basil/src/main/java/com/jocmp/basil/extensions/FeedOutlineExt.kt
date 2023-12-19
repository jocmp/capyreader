package com.jocmp.basil.extensions

import com.jocmp.basil.Feed
import com.jocmp.basil.db.Feeds as DBFeed
import com.jocmp.basil.opml.Feed as OPMLFeed

internal fun OPMLFeed.asFeed(feeds: Map<Long, DBFeed>): Feed? {
    val parsedID = id?.toLongOrNull() ?: return null
    val feed = feeds[parsedID]

    feed ?: return null

    return Feed(
        id = feed.id.toString(),
        externalID = feed.external_id,
        name = title ?: "",
        feedURL = xmlUrl ?: ""
    )
}
