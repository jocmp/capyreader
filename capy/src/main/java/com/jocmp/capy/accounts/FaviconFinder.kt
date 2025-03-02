package com.jocmp.capy.accounts

import com.jocmp.capy.Feed
import com.jocmp.capy.articles.ArticleContent
import com.jocmp.capy.common.optionalURL
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import java.net.URL

class FaviconFinder(
    private val httpClient: OkHttpClient,
    private val faviconPolicy: FaviconFetcher
) {
    suspend fun find(feed: Feed): String? {
        val siteURL = siteURL(feed) ?: return null

        val html = ArticleContent(httpClient).fetch(siteURL).getOrNull() ?: return null

        return parse(html, baseURL = siteURL.toString())
    }

    internal suspend fun parse(html: String, baseURL: String): String? {
        val iconURLs = mutableListOf<String>()

        val doc = Jsoup.parse(html, baseURL)

        doc.select("link[rel~=icon]").forEach {
            iconURLs.add(it.attr("abs:href"))
        }

        iconURLs.add(URL(baseURL).toURI().resolve("/favicon.ico").toString())

        return iconURLs.firstOrNull { faviconPolicy.isValid(it) }
    }

    companion object {
        fun siteURL(feed: Feed): URL? {
            return optionalURL(feed.siteURL) ?: optionalURL(feed.feedURL)?.let { url ->
                optionalURL("${url.protocol}://${url.host}")
            }
        }
    }
}
