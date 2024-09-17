package com.jocmp.capy.accounts

import com.prof18.rssparser.model.RssItem
import org.jsoup.Jsoup
import java.net.URI
import java.net.URL

internal class ParsedItem(private val item: RssItem, private val siteURL: String?) {
    val contentHTML: String?
        get() {
            val currentContent = item.content.orEmpty().ifBlank {
                item.description.orEmpty()
            }

            if (currentContent.isBlank()) {
                return null
            }

            return currentContent
        }

    val summary: String?
        get() = item.description?.let {
            if (it.isBlank()) {
                null
            } else {
                Jsoup.parse(it).text()
            }
        }
    
    val title: String
        get() = Jsoup.parse(item.title.orEmpty()).text()

    val url: String? = cleanedURL(item.link)?.toString()

    val imageURL: String?
        get() = cleanedURL(item.image)?.toString()

    private fun cleanedURL(inputURL: String?): URL? {
        val url = inputURL.orEmpty()

        if (url.isBlank()) {
            return null
        }

        return try {
            val uri = URI(url)

            if (uri.isAbsolute) {
                uri.toURL()
            } else {
                URI(siteURL).resolve(uri).toURL()
            }
        } catch (e: Throwable) {
            null
        }
    }
}
