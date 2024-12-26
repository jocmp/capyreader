package com.jocmp.feedfinder

import com.jocmp.feedfinder.parser.Feed
import com.jocmp.feedfinder.sources.BodyLinks
import com.jocmp.feedfinder.sources.Guess
import com.jocmp.feedfinder.sources.MetaLinks
import com.jocmp.feedfinder.sources.Source
import com.jocmp.feedfinder.sources.XML
import com.jocmp.rssparser.RssParser
import com.jocmp.rssparser.RssParserBuilder
import com.jocmp.rssparser.model.RssChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.net.URI

class DefaultFeedFinder internal constructor(
    httpClient: OkHttpClient,
    private val request: Request = DefaultRequest(httpClient),
    private val rssParser: RssParser = RssParserBuilder(callFactory = httpClient).build()
) : FeedFinder {
    constructor(client: OkHttpClient) : this(httpClient = client)

    override suspend fun find(url: String): Result<List<Feed>> = withContext(Dispatchers.IO) {
        try {
            val parsedURL = URI(url.withProtocol).toURL()
            val response = request.fetch(url = parsedURL)
            val feeds = mutableListOf<Feed>()

            sources(response).forEach { source ->
                if (feeds.isNotEmpty()) {
                    return@forEach
                }

                val currentFeeds = source.find()

                if (currentFeeds.isNotEmpty()) {
                    feeds.addAll(currentFeeds)
                }
            }

            if (feeds.isEmpty()) {
                Result.failure(FeedError.NO_FEEDS_FOUND.asException)
            } else {
                Result.success(feeds)
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }


    override suspend fun fetch(url: String): Result<RssChannel> {
        return try {
            Result.success(rssParser.getRssChannel(url))
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    private fun sources(response: Response): List<Source> {
        return listOf(
            XML(response),
            MetaLinks(response = response, request = request),
            BodyLinks(response = response, request = request),
            Guess(response = response, request = request)
        )
    }
}
