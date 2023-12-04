package com.jocmp.feedfinder

import com.jocmp.feedfinder.source.BaseSource
import com.jocmp.feedfinder.source.MetaLink
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.MalformedURLException
import java.net.URI
import java.net.URL

class FeedFinder(val url: String) {
    // Convert URL to HTTPS if missing
    // iterate through sources
    // return best matching XML
    suspend fun find(): Result {
        try {
            val parsedURL = URI(url)

//            val urls = listOf(parsedURL) + variations.map { variation ->
//                parsedURL.resolve(variation)
//            }
//
//            val documents = coroutineScope {
//                urls.map { async { fetchDocument(it) } }
//                    .awaitAll()
//                    .filterNotNull()
//            }

//            return find(documents = documents)

            val feeds = MetaLink(BaseSource(url = parsedURL)).find()

            return Result.Success(title = "", feedURL = URL(""))
        } catch (e: MalformedURLException) {
            return Result.Failure(error = FeedError.IO_FAILURE)
        }
    }

    private fun fetchDocument(url: URI): Document? {
        return try {
            Jsoup.connect(url.toString()).get()
        } catch (e: IOException) {
            return null
        }
    }

    companion object {
        suspend fun find(feedURL: String): Result {
            return FeedFinder(url = feedURL).find()
        }


        private val variations = listOf(
            "feed",
            "rss"
        )
    }
}
