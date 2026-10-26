package com.jocmp.mercury.extractors.custom.www.phoronix.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwPhoronixComExtractor =
    extractor("www.phoronix.com") {
        title { selectors("article h1", "article header") }

        author { selectors(".author a:first-child") }

        datePublished {
            selectors(".author")
            timezone = "America/New_York"
            format = "D MMMM YYYY at hh:mm"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".content", "article")
            defaultCleaner = false

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
        }
    }
