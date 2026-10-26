package com.jocmp.mercury.extractors.custom.www.techpowerup.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwTechpowerupComExtractor =
    extractor("www.techpowerup.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { selectors(".byline address") }

        datePublished { attr(".byline time[datetime]", "datetime") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".contnt")

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }

            clean("header", "footer")
        }

        nextPageUrl { attr(".nextpage-bottom", "href") }
    }
