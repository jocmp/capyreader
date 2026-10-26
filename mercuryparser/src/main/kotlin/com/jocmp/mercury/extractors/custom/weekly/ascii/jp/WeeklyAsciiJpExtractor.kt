package com.jocmp.mercury.extractors.custom.weekly.ascii.jp

import com.jocmp.mercury.extractors.extractor

val WeeklyAsciiJpExtractor =
    extractor("weekly.ascii.jp") {
        title { selectors("article h1", "h1[itemprop=\"headline\"]") }

        author { selectors("p.author") }

        datePublished {
            timezone = "Asia/Tokyo"
            format = "YYYY年M月D日 HH:mm"
            selector("p.date")
            attr("meta[name=\"odate\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div#contents_detail", "div.article")
        }
    }
