package com.jocmp.mercury.extractors.custom.sect.iij.ad.jp

import com.jocmp.mercury.extractors.extractor

val SectIijAdJpExtractor =
    extractor("sect.iij.ad.jp") {
        title { selectors("div.title-box-inner h1", "h3") }

        author { selectors("p.post-author a", "dl.entrydate dd") }

        datePublished {
            timezone = "Asia/Tokyo"
            format = "YYYY年M月D日"
            selectors("time")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".entry-inner", "#article")

            clean("dl.entrydate")
        }
    }
