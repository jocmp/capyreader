package com.jocmp.capy.accounts.local

import com.jocmp.capy.Enclosure
import com.jocmp.capy.common.optionalURL
import com.jocmp.capy.common.unescapingHTMLCharacters
import com.jocmp.rssparser.model.RssItem
import org.jsoup.Jsoup
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist
import java.net.URI
import java.net.URL

internal class ParsedItem(private val item: RssItem, private val siteURL: String?) {
    val url: String? = articleURL()

    val id: String? = item.guid.orEmpty().ifBlank { url?.ifBlank { null } }

    val enclosures: List<Enclosure> = buildEnclosures()

    private fun buildEnclosures(): List<Enclosure> {
        val enclosures = item.enclosures.mapNotNull { enclosure ->
            val parsedUrl =
                optionalURL(enclosure.url.unescapingHTMLCharacters) ?: return@mapNotNull null
            val isAudio = enclosure.type.startsWith("audio/")

            Enclosure(
                url = parsedUrl,
                type = enclosure.type,
                itunesDurationSeconds = if (isAudio) item.itunesItemData?.duration?.parseDuration() else null,
                itunesImage = if (isAudio) item.itunesItemData?.image else null
            )
        }

        val audioEnclosures = item.audio?.let { audioUrl ->
            val parsedUrl = optionalURL(audioUrl.unescapingHTMLCharacters) ?: return@let null

            Enclosure(
                url = parsedUrl,
                type = "audio/mpeg",
                itunesDurationSeconds = item.itunesItemData?.duration?.parseDuration(),
                itunesImage = item.itunesItemData?.image
            )
        }

        return (enclosures + listOfNotNull(audioEnclosures)).distinctBy { it.url.toString() }
    }

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

/**
 * Parses iTunes duration which can be:
 * - Seconds only: "3122"
 * - MM:SS format: "52:02"
 * - HH:MM:SS format: "02:02:35"
 */
private fun String.parseDuration(): Long? {
    val parts = split(":").mapNotNull { it.toLongOrNull() }

    return when (parts.size) {
        1 -> parts[0]
        2 -> parts[0] * 60 + parts[1]
        3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
        else -> null
    }
}
