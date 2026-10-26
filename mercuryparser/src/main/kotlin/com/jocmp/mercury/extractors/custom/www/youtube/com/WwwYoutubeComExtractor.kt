package com.jocmp.mercury.extractors.custom.www.youtube.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwYoutubeComExtractor =
    extractor("www.youtube.com") {
        title {
            attr("meta[name=\"title\"]", "value")
            selector(".watch-title")
            selector("h1.watch-title-container")
        }

        author {
            attr("link[itemprop=\"name\"]", "content")
            selector(".yt-user-info")
        }

        datePublished { attr("meta[itemProp=\"datePublished\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selector("#player-container-outer")
            selector("ytd-expandable-video-description-body-renderer #description")
            compound("#player-api", "#description")
            defaultCleaner = false

            transform("#player-api") { node, doc ->
                val videoId = doc("meta[itemProp=\"videoId\"]").attr("value").orEmpty()
                node.elements.firstOrNull()?.html(
                    "\n          <iframe src=\"https://www.youtube.com/embed/$videoId\" frameborder=\"0\" allowfullscreen></iframe>",
                )
                TransformResult.NoChange
            }
            transform("#player-container-outer") { node, doc ->
                val videoId = doc("meta[itemProp=\"videoId\"]").attr("value").orEmpty()
                val description = doc("meta[itemProp=\"description\"]").attr("value").orEmpty()
                val html =
                    "\n        <iframe src=\"https://www.youtube.com/embed/$videoId\" " +
                        "frameborder=\"0\" allowfullscreen></iframe>" +
                        "\n        <div><span>$description</span></div>"
                node.elements.firstOrNull()?.html(html)
                TransformResult.NoChange
            }
        }
    }
