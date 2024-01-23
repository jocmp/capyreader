package com.jocmp.feedfinder

import com.jocmp.feedfinder.parser.Feed
import com.jocmp.feedfinder.sources.BodyLinks
import com.jocmp.feedfinder.sources.MetaLinks
import com.jocmp.feedfinder.sources.Source
import com.jocmp.feedfinder.sources.XML
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException

class FeedFinder internal constructor(
    private val url: String,
    private val request: Request
) {
    constructor(url: String): this(url, DefaultRequest())

    suspend fun find(): Result = withContext(Dispatchers.IO) {
        try {
            val parsedURL = URI(url.withProtocol).toURL()
            val response = request.fetch(url = parsedURL)
            val feeds = mutableListOf<Feed>()

            sources(response).forEach { source ->
                val currentFeeds = source.find()

                if (currentFeeds.isNotEmpty()) {
                    feeds.addAll(currentFeeds)
                    return@forEach
                }
            }

            Result.Success(feeds = feeds)
        } catch (e: MalformedURLException) {
            Result.Failure(error = FeedError.IO_FAILURE)
        } catch (e: URISyntaxException) {
            Result.Failure(error = FeedError.INVALID_URL)
        }
    }

    private fun sources(response: Response): List<Source> {
        return listOf(
            XML(response),
            MetaLinks(response = response, request = request),
            BodyLinks(response = response, request = request),
        )
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

val String.withProtocol: String
    get() {
        return if (!(startsWith("http") || startsWith("https"))) {
            "https://$this"
        } else {
            this
        }
    }
