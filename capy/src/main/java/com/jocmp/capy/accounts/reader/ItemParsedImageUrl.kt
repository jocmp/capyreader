package com.jocmp.capy.accounts.reader

import com.jocmp.capy.common.unescapingHTMLCharacters
import com.jocmp.readerclient.Item
import org.jsoup.Jsoup

internal fun parsedImageURL(item: Item): String? {
    val imageHref = item.image?.href

    if (imageHref != null) {
        return imageHref.unescapingHTMLCharacters
    }

    val content = item.summary.content

    return Jsoup.parse(content).selectFirst("img")?.attr("src")
}
