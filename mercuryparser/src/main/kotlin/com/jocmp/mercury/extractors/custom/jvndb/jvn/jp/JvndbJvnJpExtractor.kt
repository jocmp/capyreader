package com.jocmp.mercury.extractors.custom.jvndb.jvn.jp

import com.jocmp.mercury.extractors.extractor

val JvndbJvnJpExtractor =
    extractor("jvndb.jvn.jp") {
        title { selectors("title") }

        datePublished {
            selectors("div.modifytxt:nth-child(2)")
            timezone = "Asia/Tokyo"
            format = "YYYY/M/D"
        }

        content {
            selectors("#news-list")
            defaultCleaner = false
        }
    }
