package com.jocmp.mercury.extractors.custom.medium.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor
import org.jsoup.nodes.TextNode
import java.net.URLDecoder

private val YT_REGEX =
    Regex("""https://i\.embed\.ly/.+url=https://i\.ytimg\.com/vi/(\w+)/""")

val MediumComExtractor =
    extractor("medium.com") {
        title {
            selector("h1")
            attr("meta[name=\"og:title\"]", "value")
        }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article")

            // Allow drop cap character.
            transform("section span:first-of-type") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val text = el.html()
                if (text.length == 1 && Regex("""^[a-zA-Z()]+$""").matches(text)) {
                    el.replaceWith(TextNode(text))
                }
                TransformResult.NoChange
            }

            // Re-write lazy-loaded youtube videos.
            transform("iframe") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val thumbRaw = el.attr("data-thumbnail")
                val thumb =
                    if (thumbRaw.isNotEmpty()) {
                        runCatching { URLDecoder.decode(thumbRaw, "UTF-8") }.getOrElse { thumbRaw }
                    } else {
                        ""
                    }
                val parentFigure = generateSequence(el.parent()) { it.parent() }.firstOrNull { it.tagName() == "figure" }
                val match = YT_REGEX.find(thumb)
                if (match != null) {
                    val youtubeId = match.groupValues[1]
                    el.attr("src", "https://www.youtube.com/embed/$youtubeId")
                    if (parentFigure != null) {
                        val caption = parentFigure.selectFirst("figcaption")
                        parentFigure.empty()
                        parentFigure.appendChild(el)
                        if (caption != null) parentFigure.appendChild(caption)
                    }
                } else {
                    parentFigure?.remove()
                }
                TransformResult.NoChange
            }

            // Rewrite figures to pull out image and caption, remove rest.
            transform("figure") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                if (el.selectFirst("iframe") != null) return@transform TransformResult.NoChange
                val imgs = el.select("img")
                val img = imgs.lastOrNull()
                val caption = el.selectFirst("figcaption")
                el.empty()
                if (img != null) el.appendChild(img)
                if (caption != null) el.appendChild(caption)
                TransformResult.NoChange
            }

            // Remove any smaller images that did not get caught by the generic
            // image cleaner (author photo 48px, leading sentence images 79px, etc.).
            transform("img") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val width = el.attr("width").toIntOrNull()
                if (width != null && width < 100) el.remove()
                TransformResult.NoChange
            }

            clean("span a", "svg")
        }
    }
