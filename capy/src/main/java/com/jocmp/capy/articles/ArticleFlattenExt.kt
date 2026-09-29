package com.jocmp.capy.articles

import com.jocmp.capy.Article
import com.jocmp.capy.common.escapingSpecialHTMLCharacters
import com.jocmp.capy.logging.CapyLog
import com.jocmp.mallet.LinearArticle
import com.jocmp.mallet.LinearElement
import com.jocmp.mallet.LinearImage
import com.jocmp.mallet.LinearImageSource
import com.jocmp.mallet.LinearText
import com.jocmp.mallet.LinearTextBlockStyle
import com.jocmp.mallet.LinearVideo
import com.jocmp.mallet.LinearVideoSource
import com.jocmp.mallet.Mallet

fun Article.flatten(): LinearArticle {
    val baseUrl = url?.toString() ?: siteURL.orEmpty()

    val flattened = Mallet.flatten(content, baseUrl).getOrElse { error ->
        CapyLog.error("flatten_article", error, mapOf("article_id" to id))
        summaryFallback()
    }

    flattened.truncated?.let { truncation ->
        CapyLog.warn(
            "flatten_article_truncated",
            mapOf(
                "article_id" to id,
                "elements" to truncation.elementCount.toString(),
                "chars" to truncation.charCount.toString(),
            )
        )
    }

    return flattened.copy(elements = flattened.elements + enclosureElements())
}

private fun Article.summaryFallback(): LinearArticle {
    if (summary.isBlank()) {
        return LinearArticle(elements = emptyList())
    }

    return LinearArticle(
        elements = listOf(
            LinearText(
                ids = emptySet(),
                text = summary,
                blockStyle = LinearTextBlockStyle.TEXT,
                annotations = emptyList(),
            )
        )
    )
}

private fun Article.enclosureElements(): List<LinearElement> {
    val images = enclosures
        .filter { it.type.startsWith("image/") }
        .filterNot { enclosure ->
            val url = enclosure.url.toString()

            content.contains(url) || content.contains(url.escapingSpecialHTMLCharacters)
        }
        .map { enclosure ->
            LinearImage(
                ids = emptySet(),
                sources = listOf(
                    LinearImageSource(
                        imgUri = enclosure.url.toString(),
                        widthPx = null,
                        heightPx = null,
                        pixelDensity = null,
                        screenWidth = null,
                    )
                ),
                caption = null,
                link = null,
            )
        }

    val videos = enclosures
        .filter { it.type.startsWith("video/") }
        .map { enclosure ->
            LinearVideo(
                ids = emptySet(),
                sources = listOf(
                    LinearVideoSource(
                        uri = enclosure.url.toString(),
                        link = enclosure.url.toString(),
                        imageThumbnail = null,
                        widthPx = null,
                        heightPx = null,
                        mimeType = enclosure.type,
                    )
                ),
            )
        }

    return images + videos
}
