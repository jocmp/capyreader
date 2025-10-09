package com.jocmp.capy.accounts.reader

import com.jocmp.capy.Enclosure
import com.jocmp.capy.common.optionalURL
import com.jocmp.capy.common.unescapingHTMLCharacters
import com.jocmp.readerclient.Item
import org.jsoup.Jsoup

internal object ReaderEnclosureParsing {
    internal fun parsedImageURL(item: Item): String? {
        val imageHref = item.images.firstOrNull()?.href

        if (imageHref != null) {
            return imageHref.unescapingHTMLCharacters
        }

        val content = item.summary.content.orEmpty()

        return Jsoup.parse(content).selectFirst("img")?.attr("src")
    }

    internal fun validEnclosures(item: Item): List<Enclosure> {
        return item.enclosure.orEmpty().mapNotNull(::toEnclosure)
    }

    private fun toEnclosure(enclosure: Item.Enclosure): Enclosure? {
        val type = enclosure.type ?: return null
        val url = optionalURL(enclosure.href?.unescapingHTMLCharacters) ?: return null

        return Enclosure(url = url, type = type, itunesDurationSeconds = null, itunesImage = null)
    }
}
