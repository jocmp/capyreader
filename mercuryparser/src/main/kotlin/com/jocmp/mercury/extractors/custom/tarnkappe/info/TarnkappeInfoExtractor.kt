package com.jocmp.mercury.extractors.custom.tarnkappe.info

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val TarnkappeInfoExtractor =
    extractor("tarnkappe.info") {
        title { selectors("title", "h1") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("main")

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }

            clean("section#author")
        }
    }
