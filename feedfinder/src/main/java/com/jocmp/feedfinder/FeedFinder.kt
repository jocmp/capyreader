package com.jocmp.feedfinder

import com.jocmp.feedfinder.parser.Feed
import com.jocmp.feedfinder.sources.BodyLinks
import com.jocmp.feedfinder.sources.MetaLinks
import com.jocmp.feedfinder.sources.Source
import com.jocmp.feedfinder.sources.XML
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException

class FeedFinder internal constructor(
    private val url: String,
    private val request: Request
) {
    constructor(url: String) : this(url, DefaultRequest())

    suspend fun find(): Result<List<Feed>> = withContext(Dispatchers.IO) {
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

            if (feeds.isEmpty()) {
                Result.failure(FeedError.NO_FEEDS_FOUND.asException)
            } else {
                Result.success(feeds)
            }
        } catch (e: MalformedURLException) {
            Result.failure(e)
        } catch (e: URISyntaxException) {
            Result.failure(FeedError.INVALID_URL.asException)
        } catch (e: FileNotFoundException) {
            Result.failure(FeedError.INVALID_URL.asException)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    private fun sources(response: Response): List<Source> {
        return listOf(
            XML(response),
            MetaLinks(response = response, request = request),
            BodyLinks(response = response, request = request),
        )
    }

    companion object {
        suspend fun find(feedURL: String): Result<List<Feed>> {
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
