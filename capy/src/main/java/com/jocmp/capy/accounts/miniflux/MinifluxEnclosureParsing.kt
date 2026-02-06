package com.jocmp.capy.accounts.miniflux

import com.jocmp.minifluxclient.Entry
import org.jsoup.Jsoup

internal object MinifluxEnclosureParsing {
    internal fun parsedImageURL(entry: Entry): String? {
        val imageEnclosure = entry.enclosures
            ?.firstOrNull { it.mime_type.startsWith("image/") }

        if (imageEnclosure != null) {
            return imageEnclosure.url
        }

        return Jsoup.parse(entry.content).selectFirst("img")?.attr("src")
    }
}
