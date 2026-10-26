package com.jocmp.mercury.extractors.custom.www.asahi.com

import com.jocmp.mercury.extractors.extractor

val WwwAsahiComExtractor =
    extractor("www.asahi.com") {
        title { selectors("main h1", ".ArticleTitle h1") }

        author { attr("meta[name=\"article:author\"]", "value") }

        datePublished { attr("meta[name=\"pubdate\"]", "value") }

        excerpt { attr("meta[name=\"og:description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("main")

            defaultCleaner = false

            clean("div.AdMod", "div.LoginSelectArea", "time", "div.notPrint")
        }
    }
