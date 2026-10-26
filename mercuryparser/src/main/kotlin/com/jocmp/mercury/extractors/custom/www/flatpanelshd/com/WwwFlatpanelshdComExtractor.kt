package com.jocmp.mercury.extractors.custom.www.flatpanelshd.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwFlatpanelshdComExtractor =
    extractor("www.flatpanelshd.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { attr("meta[itemprop=\"author\"]", "value") }

        datePublished { attr("meta[itemprop=\"datePublished\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("#zephr-anchor", "article")

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("h3") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("h4") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("pre", renameTo = "div")
        }
    }
