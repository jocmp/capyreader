package com.jocmp.mercury.extractors.custom.www.infoq.com

import com.jocmp.mercury.extractors.extractor

val WwwInfoqComExtractor =
    extractor("www.infoq.com") {
        title { selectors("h1.heading") }

        author { selectors("div.widget.article__authors") }

        datePublished {
            selectors(".article__readTime.date")
            timezone = "Asia/Tokyo"
            format = "YYYY年M月D日"
        }

        dek { attr("meta[name=\"og:description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.article__data")
            defaultCleaner = false
        }
    }
