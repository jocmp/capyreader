package com.jocmp.capy.accounts.local

import com.jocmp.capy.Enclosure
import com.jocmp.capy.common.optionalURL
import com.jocmp.capy.common.unescapingHTMLCharacters
import com.jocmp.rssparser.model.RssItem
import com.jocmp.rssparser.model.RssItemEnclosure
import org.jsoup.Jsoup
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist
import java.net.URI
import java.net.URL

internal class ParsedItem(private val item: RssItem, private val siteURL: String?) {
    val url: String? = articleURL()

    val id: String? = item.guid.orEmpty().ifBlank { url?.ifBlank { null } }

    val enclosures = item.enclosures.mapNotNull { it.toEnclosure() }

    val contentHTML: String?
        get() {
            val mediaContent = RichMedia.parse(item)

            if (mediaContent != null) {
                return mediaContent
            }

            val itemContent = item.content

            if (itemContent.isNullOrBlank()) {
                return item.description
            }

            return itemContent
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
        get() {
            val cleaner = Cleaner(Safelist.none())

            return cleaner.clean(Jsoup.parse(item.title.orEmpty())).text()
        }

    val imageURL: String?
        get() = item.media?.thumbnailUrl ?: cleanedURL(item.image)?.toString()

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

private fun RssItemEnclosure.toEnclosure(): Enclosure? {
    val url = optionalURL(url.unescapingHTMLCharacters) ?: return null

    return Enclosure(url = url, type = type, itunesDurationSeconds = null, itunesImage = null)
}
