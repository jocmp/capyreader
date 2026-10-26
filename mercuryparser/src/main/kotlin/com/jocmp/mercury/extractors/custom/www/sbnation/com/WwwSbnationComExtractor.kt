package com.jocmp.mercury.extractors.custom.www.sbnation.com

import com.jocmp.mercury.extractors.extractor

val WwwSbnationComExtractor =
    extractor("www.sbnation.com") {
        title { selectors("h1.c-page-title") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { selectors("p.c-entry-summary.p-dek", "h2.c-entry-summary.p-dek") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.c-entry-content")
        }
    }
