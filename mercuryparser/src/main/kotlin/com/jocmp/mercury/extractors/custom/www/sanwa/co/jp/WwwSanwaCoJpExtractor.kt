package com.jocmp.mercury.extractors.custom.www.sanwa.co.jp

import com.jocmp.mercury.extractors.extractor

val WwwSanwaCoJpExtractor =
    extractor("www.sanwa.co.jp") {
        title { selectors("#newsContent h1") }

        datePublished {
            selectors("dl.date")
            timezone = "Asia/Tokyo"
            format = "YYYY.M.D"
        }

        dek { attr("meta[name=\"og:description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("#newsContent")
            defaultCleaner = false
            clean("#smartphone", "div.sns_box", "div.contentFoot")
        }
    }
