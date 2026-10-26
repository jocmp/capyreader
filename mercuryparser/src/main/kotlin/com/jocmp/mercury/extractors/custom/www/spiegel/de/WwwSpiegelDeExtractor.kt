package com.jocmp.mercury.extractors.custom.www.spiegel.de

import com.jocmp.mercury.extractors.extractor

val WwwSpiegelDeExtractor =
    extractor("www.spiegel.de") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"date\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div[data-area=\"body\"]", "article")
        }
    }
