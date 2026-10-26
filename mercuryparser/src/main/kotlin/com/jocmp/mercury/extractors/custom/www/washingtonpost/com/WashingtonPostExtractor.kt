package com.jocmp.mercury.extractors.custom.www.washingtonpost.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

// Port note: parity test fails on date_published. The fixture's
// .author-timestamp content attribute is `2018-10-29T10:15-500` (3-digit offset).
// moment.js (upstream) parses this; the Kotlin cleaner's date parsers require a
// 4-digit ±HHMM offset, so cleanDatePublished returns null. The selector itself
// matches upstream exactly — issue lives in shared cleaner infrastructure.
val WashingtonPostExtractor =
    extractor("www.washingtonpost.com") {
        title { selectors("h1", "#topper-headline-wrapper") }

        author { selectors(".pb-author-name") }

        datePublished { attr(".author-timestamp[itemprop=\"datePublished\"]", "content") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".article-body")

            transform("div.inline-content") { node, _ ->
                if (node.find("img,iframe,video").length > 0) {
                    return@transform TransformResult.Rename("figure")
                }

                node.remove()
                TransformResult.NoChange
            }
            transform(".pb-caption", renameTo = "figcaption")

            clean(".interstitial-link", ".newsletter-inline-unit")
        }
    }
