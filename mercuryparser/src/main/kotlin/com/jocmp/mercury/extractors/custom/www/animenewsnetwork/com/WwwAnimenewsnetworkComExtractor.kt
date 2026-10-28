package com.jocmp.mercury.extractors.custom.www.animenewsnetwork.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor
import org.jsoup.nodes.Element

private const val MEDIA = "img, picture, video, audio, iframe, svg, embed, object"

val WwwAnimenewsnetworkComExtractor =
    extractor("www.animenewsnetwork.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        datePublished { attr("small time", "datetime") }

        dek { attr("meta[name=\"description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".KonaBody")

            // .KonaBody is only the article, and its encyclopedia links push the
            // body past the conditional cleaner's link density limit.
            defaultCleaner = false

            // Lazy images keep the real URL in data-src and a spacer.gif in src
            transform("img") { node, _ ->
                node.attr("data-src")?.let { dataSrc ->
                    node.attr("src", dataSrc)
                    node.removeAttr("data-src")
                }
                TransformResult.NoChange
            }

            // Reviews lay out the cover, synopsis, and body in decorative tables
            transform("table.layout") { node, _ ->
                node.elements.forEach(::unwrapLayoutTable)
                TransformResult.NoChange
            }

            clean(
                "instaread-player",
                // Hidden duplicate of #small-synopsis
                "#big-synopsis",
                // Episode rating widgets and episode navigation
                "div:has(> h3 .star_rating)",
                "p:has(.star_rating)",
                "div:has(> p > select.js-content)",
            )
        }
    }

private fun unwrapLayoutTable(table: Element) {
    val content = Element("div")

    table.select("td")
        .filter { cell -> cell.parents().firstOrNull { it.tagName() == "table" } == table }
        .forEach { cell ->
            if (cell.hasClass("horizontal")) {
                cell.text().trim().takeIf { it.isNotEmpty() }?.let { label ->
                    content.appendElement("p").appendElement("strong").text(label)
                }
            } else if (cell.text().isNotBlank() || cell.selectFirst(MEDIA) != null) {
                cell.childNodes().toList().forEach { content.appendChild(it) }
            }
        }

    table.replaceWith(content)
}
