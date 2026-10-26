package com.jocmp.mercury.extractors.custom.www.recode.net

import com.jocmp.mercury.extractors.extractor

val WwwRecodeNetExtractor =
    extractor("www.recode.net") {
        title { selectors("h1.c-page-title") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { selectors("h2.c-entry-summary.p-dek") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            compound("figure.e-image--hero", ".c-entry-content")
            selector(".c-entry-content")
        }
    }
