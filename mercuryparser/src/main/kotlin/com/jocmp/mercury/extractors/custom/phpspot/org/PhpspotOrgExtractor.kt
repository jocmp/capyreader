package com.jocmp.mercury.extractors.custom.phpspot.org

import com.jocmp.mercury.extractors.extractor

val PhpspotOrgExtractor =
    extractor("phpspot.org") {
        title { selectors("h3.hl") }

        datePublished {
            timezone = "Asia/Tokyo"
            format = "YYYY年M月D日"
            selectors("h4.hl")
        }

        content {
            selectors("div.entrybody")

            defaultCleaner = false
        }
    }
