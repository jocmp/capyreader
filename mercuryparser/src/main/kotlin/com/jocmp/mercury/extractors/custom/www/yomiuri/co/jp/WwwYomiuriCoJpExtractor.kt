package com.jocmp.mercury.extractors.custom.www.yomiuri.co.jp

import com.jocmp.mercury.extractors.extractor

val WwwYomiuriCoJpExtractor =
    extractor("www.yomiuri.co.jp") {
        title { selectors("h1.title-article.c-article-title") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.p-main-contents")
        }
    }
