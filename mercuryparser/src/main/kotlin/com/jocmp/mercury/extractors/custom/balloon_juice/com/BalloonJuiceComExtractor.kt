@file:Suppress("ktlint:standard:package-name")

package com.jocmp.mercury.extractors.custom.balloon_juice.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val BalloonJuiceComExtractor =
    extractor("balloon-juice.com") {
        title { selectors("h1.entry-title") }

        author { selectors(".entry-author-name") }

        datePublished {
            attr("meta[property=\"article:published_time\"]", "content")
            attr("meta[name=\"article:published_time\"]", "value")
        }

        leadImageUrl {
            attr("meta[property=\"og:image\"]", "content")
            attr("meta[name=\"og:image\"]", "value")
        }

        content {
            selectors(".entry-content", "article")

            // Handle JS-rendered iframes
            transform("iframe[src*=\"embed.bsky.app\"]") { node, _ ->
                node.addClass("mercury-parser-keep iframe-embed-bsky")
                node.parent(".bluesky-embed").addClass("mercury-parser-keep")
                TransformResult.NoChange
            }
            // Handle no-JS blockquote fallbacks - convert to iframes
            transform("blockquote.bluesky-embed[data-bluesky-uri]") { node, _ ->
                val uri = node.attr("data-bluesky-uri")
                if (uri != null) {
                    // Convert at://did:plc:.../app.bsky.feed.post/... to embed URL
                    val embedPath = uri.replace("at://", "")
                    val src = "https://embed.bsky.app/embed/$embedPath"
                    node.replaceWith(
                        "<iframe src=\"$src\" class=\"mercury-parser-keep iframe-embed-bsky\" " +
                            "width=\"100%\" frameborder=\"0\"></iframe>",
                    )
                }
                TransformResult.NoChange
            }

            clean(".shared-counts-wrap", ".entry-meta")
        }
    }
