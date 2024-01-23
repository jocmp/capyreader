package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.Response
import com.jocmp.feedfinder.parser.Feed
import com.jocmp.feedfinder.parser.Parser.Result.ParsedFeed

internal class XML(private val response: Response): Source {
    override suspend fun find(): List<Feed> {
        val result = response.parse()

        if (result is ParsedFeed && result.feed.isValid()) {
            return listOf(result.feed)
        }

        return emptyList()
    }
}
