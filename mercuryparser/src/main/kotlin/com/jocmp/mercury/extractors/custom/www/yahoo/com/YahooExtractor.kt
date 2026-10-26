package com.jocmp.mercury.extractors.custom.www.yahoo.com

import com.jocmp.mercury.extractors.extractor

val YahooExtractor =
    extractor("www.yahoo.com") {
        title { selectors("header.canvas-header") }

        author { selectors("span.provider-name") }

        datePublished { attr("time.date[datetime]", "datetime") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".content-canvas")
            clean(".figure-caption")
        }
    }
