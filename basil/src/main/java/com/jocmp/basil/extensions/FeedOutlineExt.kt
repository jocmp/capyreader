package com.jocmp.basil.extensions

import com.jocmp.basil.Feed
import com.jocmp.basil.opml.Outline

internal val Outline.FeedOutline.asFeed: Feed
    get() {
        return Feed(
            id = "",
            name = feed.title ?: "",
            feedURL = feed.xmlUrl ?: ""
        )
    }
