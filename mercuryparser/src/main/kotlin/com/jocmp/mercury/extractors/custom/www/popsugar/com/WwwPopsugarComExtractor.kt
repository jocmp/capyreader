package com.jocmp.mercury.extractors.custom.www.popsugar.com

import com.jocmp.mercury.extractors.extractor

val WwwPopsugarComExtractor =
    extractor("www.popsugar.com") {
        title { selectors("h2.post-title", "title-text") }

        author { attr("meta[name=\"article:author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("#content")
            clean(".share-copy-title", ".post-tags", ".reactions")
        }
    }
