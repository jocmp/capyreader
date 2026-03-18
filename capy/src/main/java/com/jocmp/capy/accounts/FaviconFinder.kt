package com.jocmp.capy.accounts

import com.jocmp.capy.Feed
import com.jocmp.capy.articles.ArticleContent
import com.jocmp.capy.common.baseURL
import com.jocmp.capy.common.optionalURL
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import java.net.URL

class FaviconFinder(
    private val httpClient: OkHttpClient,
    private val faviconPolicy: FaviconPolicy,
    private val userAgent: String = "",
    private val acceptLanguage: String = "",
) {
    suspend fun find(url: URL): String? {
        val html = ArticleContent(httpClient, userAgent, acceptLanguage).fetch(url).getOrNull()
            ?: return null

        return parse(html, baseURL = url.toString())
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
            return optionalURL(feed.siteURL)?.baseURL() ?: optionalURL(feed.feedURL)?.baseURL()
        }
    }
}
