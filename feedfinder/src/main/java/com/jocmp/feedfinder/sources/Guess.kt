package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.DefaultRequest
import com.jocmp.feedfinder.Request
import com.jocmp.feedfinder.Response
import com.jocmp.feedfinder.parser.Feed
import com.jocmp.feedfinder.toParsedFeed
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class Guess(
    private val response: Response,
    private val request: Request = DefaultRequest()
) : Source {
    override suspend fun find(): List<Feed> {
        return coroutineScope {
            listOf("/rss", "/feed")
                .map { path ->
                    val guess = response.url.toURI().resolve(path).toURL()
                    async { request.fetch(url = guess) }
                }
                .awaitAll()
                .mapNotNull { it.toParsedFeed() }
        }
    }
}
