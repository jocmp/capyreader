package com.jocmp.feedfinder

import com.jocmp.feedfinder.parser.Feed
import com.jocmp.rssparser.model.RssChannel

interface FeedFinder {
    suspend fun find(url: String): Result<List<Feed>>

    suspend fun fetch(url: String): Result<RssChannel>
}
