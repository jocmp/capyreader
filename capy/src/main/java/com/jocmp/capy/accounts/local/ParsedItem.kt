package com.jocmp.capy.accounts.local

import com.jocmp.capy.common.escapingHTMLCharacters
import com.jocmp.rssparser.model.RssItem
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import java.net.URI
import java.net.URL

internal class ParsedItem(private val item: RssItem, private val siteURL: String?) {
    val url: String? = articleURL()

    val id: String? = url ?: item.guid

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
                Jsoup.clean(it, Safelist.none())
            }
        }

    val title: String
        get() = Jsoup.parse(item.title.orEmpty()).text().escapingHTMLCharacters

    val imageURL: String?
        get() = cleanedURL(item.image)?.toString()

    private fun articleURL(): String? {
        val link = cleanedURL(item.link) ?: return null

        return ArticleURL.parse(link).toString()
    }

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
