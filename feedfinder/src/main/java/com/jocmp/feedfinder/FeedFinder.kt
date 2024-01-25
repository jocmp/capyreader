package com.jocmp.feedfinder

import com.jocmp.feedfinder.parser.Feed

interface FeedFinder {
    suspend fun find(url: String): Result<List<Feed>>
}
