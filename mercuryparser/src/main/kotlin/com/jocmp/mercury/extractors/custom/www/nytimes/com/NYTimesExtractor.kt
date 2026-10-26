package com.jocmp.mercury.extractors.custom.www.nytimes.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val NYTimesExtractor =
    extractor("www.nytimes.com") {
        title {
            selectors(
                "h1[data-testid=\"headline\"]",
                "h1.g-headline",
                "h1[itemprop=\"headline\"]",
                "h1.headline",
                "h1 .balancedHeadline",
            )
        }

        author {
            attr("meta[name=\"author\"]", "value")
            selector(".g-byline")
            selector(".byline")
            attr("meta[name=\"byl\"]", "value")
        }

        content {
            selectors("div.g-blocks", "section[name=\"articleBody\"]", "article#story")

            transform("img.g-lazy") { node, _ ->
                val src = node.attr("src")
                if (src != null) {
                    val width = 640
                    node.attr("src", src.replace("{{size}}", width.toString()))
                }
                TransformResult.NoChange
            }

            clean(
                ".ad",
                "header#story-header",
                ".story-body-1 .lede.video",
                ".visually-hidden",
                "#newsletter-promo",
                ".promo",
                ".comments-button",
                ".hidden",
                ".comments",
                ".supplemental",
                ".nocontent",
                ".story-footer-links",
            )
        }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            attr("meta[name=\"article:published\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }
    }
