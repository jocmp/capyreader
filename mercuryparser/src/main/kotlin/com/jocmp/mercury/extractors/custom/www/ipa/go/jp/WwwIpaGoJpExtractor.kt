package com.jocmp.mercury.extractors.custom.www.ipa.go.jp

import com.jocmp.mercury.extractors.extractor

val WwwIpaGoJpExtractor =
    extractor("www.ipa.go.jp") {
        title { selectors("h1") }

        datePublished {
            selectors("p.ipar_text_right")
            timezone = "Asia/Tokyo"
            format = "YYYY年M月D日"
        }

        content {
            selectors("#ipar_main")
            defaultCleaner = false
            clean("p.ipar_text_right")
        }
    }
