package com.jocmp.capy

import com.jocmp.feedfinder.FeedFinder
import com.jocmp.feedfinder.parser.Feed
import com.jocmp.rssparser.model.ConditionalGetInfo
import com.jocmp.rssparser.model.RssChannel
import com.jocmp.rssparser.model.RssChannelResult
import com.jocmp.rssparser.model.RssImage

class MockFeedFinder(private val sites: Map<String, Feed> = emptyMap()) : FeedFinder {
    override suspend fun find(url: String): Result<List<Feed>> {
        val feed = sites[url] ?: return Result.failure(Throwable("No feeds!"))

        return Result.success(listOf(feed))
    }

    override suspend fun fetch(url: String, conditionalGet: ConditionalGetInfo): Result<RssChannelResult> {
        val feed = sites[url] ?: return Result.failure(Throwable("No feeds!"))

        val channel = RssChannel(
            title = feed.name,
            link = feed.siteURL?.toString(),
            description = null,
            image = feed.faviconURL?.let { RssImage(url = it.toString()) },
            lastBuildDate = null,
            updatePeriod = null,
            items = feed.items,
            itunesChannelData = null,
        )

        return Result.success(RssChannelResult(channel = channel, conditionalGet = ConditionalGetInfo.EMPTY))
    }
}
