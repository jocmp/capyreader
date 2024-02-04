package com.jocmp.basil

import com.jocmp.feedfinder.FeedFinder
import java.net.URL

class FeedSearch(private val feedFinder: FeedFinder) {
    suspend fun search(url: String): Result<SearchResult>  {
        return feedFinder.find(url = url).fold(
            onSuccess = {
                val feed = it.first()

                Result.success(
                    SearchResult(
                        url = feed.feedURL,
                        siteURL = feed.siteURL,
                        name = feed.name,
                    )
                )
            },
            onFailure = {
                Result.failure(it)
            }
        )
    }

    data class SearchResult(
        val url: URL,
        val name: String = "",
        val siteURL: URL? = null,
    )
}
