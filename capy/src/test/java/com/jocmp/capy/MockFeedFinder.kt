package com.jocmp.capy

import com.jocmp.feedfinder.FeedFinder
import com.jocmp.feedfinder.parser.Feed
import com.jocmp.rssparser.model.RssChannel

class MockFeedFinder(private val sites: Map<String, Feed> = emptyMap()) : FeedFinder {
    override suspend fun find(url: String): Result<List<Feed>> {
        val feed = sites[url] ?: return Result.failure(Throwable("No feeds!"))

        return Result.success(listOf(feed))
    }

    override suspend fun fetch(url: String): Result<RssChannel> {
        TODO("Not yet implemented")
    }
}
