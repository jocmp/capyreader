package com.jocmp.feedfinder

import com.jocmp.feedfinder.parser.Feed
import com.jocmp.feedfinder.sources.XMLSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.MalformedURLException
import java.net.URI

// Parser
// - XMLFeed
// - JSONFeed
// - HTML
//   - Meta Links
//   - Body Links
// - XMLFeedGuess (second pass)

// XML can be parsed directly if XML feed
// HTML takes response body

class FeedFinder internal constructor(
    val url: String,
    private val request: Request = DefaultRequest()
) {
    // Convert URL to HTTPS if missing
    // 1. Download the request using a Java HTTP connection
    // 2. If the response is an XML Feed itself, return
    // 3. If the response is HTML and th
    internal suspend fun find(): Result = withContext(Dispatchers.IO) {
        try {
            // TODO:
            //  normalize URL via
            //  https://github.com/Ranchero-Software/RSCore/blob/a2f711d64af8f1baefdf0092f57a7f0df7f0e5e8/Sources/RSCore/Shared/String+RSCore.swift#L114
            val parsedURL = URI(url).toURL()
            val response = request.fetch(url = parsedURL)
            val feeds = mutableListOf<Feed>()

            XMLSource(response).find().let {
                if (it.isNotEmpty()) {
                    feeds.addAll(it)
                }
            }

            Result.Success(feeds = feeds)
        } catch (e: MalformedURLException) {
            Result.Failure(error = FeedError.IO_FAILURE)
        }
    }

    sealed class Result {
        class Success(val feeds: List<Feed>) : Result()

        class Failure(val error: FeedError) : Result()
    }

    companion object {
        suspend fun find(feedURL: String): Result {
            return FeedFinder(url = feedURL).find()
        }
    }
}
