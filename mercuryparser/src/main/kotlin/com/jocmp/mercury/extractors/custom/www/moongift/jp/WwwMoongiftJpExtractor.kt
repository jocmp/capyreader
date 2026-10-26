package com.jocmp.mercury.extractors.custom.www.moongift.jp

import com.jocmp.mercury.extractors.extractor

val WwwMoongiftJpExtractor =
    extractor("www.moongift.jp") {
        title { selectors("h1.title a") }

        datePublished {
            selectors("ul.meta li:not(.social):first-of-type")
            timezone = "Asia/Tokyo"
        }

        dek { attr("meta[name=\"og:description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("#main")
            clean("ul.mg_service.cf")
        }
    }
