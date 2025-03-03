package com.jocmp.capy.accounts

import com.jocmp.capy.Feed
import com.jocmp.capy.articles.ArticleContent
import com.jocmp.capy.common.optionalURL
import okhttp3.OkHttpClient
import org.jsoup.Jsoup

class FaviconFinder(
    private val httpClient: OkHttpClient,
    private val faviconPolicy: FaviconFetcher
) {
    suspend fun favicon(feed: Feed): String? {
        val iconURLs = mutableListOf<String>()

        val siteURL = optionalURL(feed.siteURL) ?: optionalURL(feed.feedURL)?.let { url ->
            optionalURL("${url.protocol}://${url.host}")
        }

        siteURL ?: return null

        val result = ArticleContent(httpClient).fetch(siteURL).getOrNull() ?: return null

        val html = Jsoup.parse(result)

        html.select("link[rel~=icon]").forEach {
            iconURLs.add(it.attr("abs:href"))
        }

        iconURLs.add(siteURL.toURI().resolve("/favicon.ico").toString())

        return iconURLs.first { faviconPolicy.isValid(it) }
    }
}
