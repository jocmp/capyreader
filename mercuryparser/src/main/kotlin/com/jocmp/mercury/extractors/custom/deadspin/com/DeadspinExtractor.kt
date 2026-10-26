package com.jocmp.mercury.extractors.custom.deadspin.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val DeadspinExtractor =
    extractor("deadspin.com") {
        supportedDomains =
            listOf(
                "jezebel.com",
                "lifehacker.com",
                "kotaku.com",
                "gizmodo.com",
                "jalopnik.com",
                "kinja.com",
                "avclub.com",
                "clickhole.com",
                "splinternews.com",
                "theonion.com",
                "theroot.com",
                "thetakeout.com",
                "theinventory.com",
            )

        title { selectors("header h1", "h1.headline") }

        author { selectors("a[data-ga*=\"Author\"]", ".author") }

        content {
            selectors(".js_post-content", ".post-content", ".entry-content")

            // Is there anything in the content you selected that needs transformed
            // before it's consumable content? E.g., unusual lazy loaded images
            transform("iframe.lazyload[data-recommend-id^=\"youtube://\"]") { node, _ ->
                val id = node.attr("id")
                if (id != null) {
                    val youtubeId = id.split("youtube-").getOrNull(1)
                    if (youtubeId != null) {
                        node.attr("src", "https://www.youtube.com/embed/$youtubeId")
                    }
                }
                TransformResult.NoChange
            }

            // Is there anything that is in the result that shouldn't be?
            // The clean selectors will remove anything that matches from
            // the result
            clean(".magnifier", ".lightbox")
        }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            attr("time.updated[datetime]", "datetime")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }
    }
