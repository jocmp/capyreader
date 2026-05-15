package com.jocmp.feedfinder

import com.jocmp.feedfinder.parser.Feed
import com.jocmp.rssparser.model.ConditionalGetInfo
import com.jocmp.rssparser.model.RssChannelResult

interface FeedFinder {
    suspend fun find(url: String): Result<List<Feed>>

    suspend fun fetch(
        url: String,
        conditionalGet: ConditionalGetInfo = ConditionalGetInfo.EMPTY,
    ): Result<RssChannelResult>
}
