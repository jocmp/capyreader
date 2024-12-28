package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.Request
import com.jocmp.feedfinder.optionalURL
import com.jocmp.feedfinder.parser.Feed
import com.jocmp.feedfinder.parser.Parser

internal sealed interface Source {
    suspend fun find(): List<Feed>

    suspend fun createFromURL(url: String, fetcher: Request): Feed? {
        val parsedURL = optionalURL(url) ?: return null

        val response = fetcher.fetch(url = parsedURL)

        return when (val result = response.parse()) {
            is Parser.Result.ParsedFeed -> result.feed
            is Parser.Result.HTMLDocument -> null
        }
    }
}
