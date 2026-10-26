package com.jocmp.mercury.extractors.custom.wccftech.com

import com.jocmp.mercury.extractors.extractor

val WccftechComExtractor =
    extractor("wccftech.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { selectors("div.meta a:first-of-type") }

        datePublished {
            attr("meta[name=\"pub_date\"]", "value")
            attr("meta[name=\"article:published_time\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".content")

            clean(".democracy")
        }
    }
