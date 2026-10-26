package com.jocmp.mercury.extractors.custom.www.thedrive.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwThedriveComExtractor =
    extractor("www.thedrive.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".entry-content", "article")

            transform("img") { node, _ ->
                node.removeAttr("sizes")
                TransformResult.NoChange
            }
            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("h3") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }

            clean(
                ".product-disclosure",
                ".recurrent-newsletter-block",
                ".pw-incontent-commerce-ad",
                "#author-widgets",
            )
        }
    }
