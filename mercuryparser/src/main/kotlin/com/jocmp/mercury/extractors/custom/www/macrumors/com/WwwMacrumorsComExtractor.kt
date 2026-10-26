package com.jocmp.mercury.extractors.custom.www.macrumors.com

import com.jocmp.mercury.extractors.extractor

val WwwMacrumorsComExtractor =
    extractor("www.macrumors.com") {
        title { selectors("h1", "h1.title") }

        author { selectors("article a[rel=\"author\"]", ".author-url") }

        datePublished {
            attr("time", "datetime")
            timezone = "America/Los_Angeles"
        }

        dek { attr("meta[name=\"description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article", ".article")
        }
    }
