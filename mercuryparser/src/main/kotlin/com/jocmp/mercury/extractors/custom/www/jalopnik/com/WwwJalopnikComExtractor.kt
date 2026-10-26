package com.jocmp.mercury.extractors.custom.www.jalopnik.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwJalopnikComExtractor =
    extractor("www.jalopnik.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { attr("meta[name=\"article:author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { attr("meta[name=\"og:description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article.news-post")

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform(".slide-key") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }

            clean(".breadcrumbs", ".byline-container")
        }
    }
