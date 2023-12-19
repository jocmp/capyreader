package com.jocmp.basil.extensions

import com.jocmp.basil.Feed
import com.jocmp.basil.db.Feeds as DBFeed
import com.jocmp.basil.opml.Feed as OPMLFeed

internal fun OPMLFeed.asFeed(feeds: Map<String, DBFeed>): Feed? {
    externalID ?: return null
    val feed = feeds[externalID]

    feed ?: return null

    return Feed(
        id = feed.external_id,
        name = title ?: "",
        feedURL = xmlUrl ?: ""
    )
}
