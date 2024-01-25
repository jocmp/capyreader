package com.jocmp.basil

import com.jocmp.feedfinder.FeedFinder
import com.jocmp.feedfinder.parser.Feed

class TestFeedFinder(private val sites: Map<String, Feed>) : FeedFinder {
    override suspend fun find(url: String): Result<List<Feed>> {
        val feed = sites[url] ?: return Result.failure(Throwable("No feeds!"))

        return Result.success(listOf(feed))
    }
}
