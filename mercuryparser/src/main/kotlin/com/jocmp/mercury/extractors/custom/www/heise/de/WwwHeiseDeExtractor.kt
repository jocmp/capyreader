package com.jocmp.mercury.extractors.custom.www.heise.de

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwHeiseDeExtractor =
    extractor("www.heise.de") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"date\"]", "value") }

        dek { selectors(".a-article-header__lead") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".article-layout__content")

            transform("h3") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }

            clean(
                ".ad-mobile-group-1",
                ".branding",
                "[data-component=\"RecommendationBox\"]",
            )
        }
    }
