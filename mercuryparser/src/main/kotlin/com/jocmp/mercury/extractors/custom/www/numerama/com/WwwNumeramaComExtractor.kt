package com.jocmp.mercury.extractors.custom.www.numerama.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwNumeramaComExtractor =
    extractor("www.numerama.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { selectors(".article-header__description") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".article-content", "article")

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }

            clean(
                ".js-newsletter-block",
                ".premium-promo-alert",
                "[data-nosnippet]",
                ".ultimedia_cntr",
            )
        }
    }
