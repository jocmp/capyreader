package com.jocmp.mercury.extractors.custom.www.rbbtoday.com

import com.jocmp.mercury.extractors.extractor

val WwwRbbtodayComExtractor =
    extractor("www.rbbtoday.com") {
        title { selectors("h1") }

        author { selectors(".writer.writer-name") }

        datePublished { attr("header time", "datetime") }

        dek {
            attr("meta[name=\"description\"]", "value")
            selector(".arti-summary")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".arti-content")
            clean(".arti-giga")
        }
    }
