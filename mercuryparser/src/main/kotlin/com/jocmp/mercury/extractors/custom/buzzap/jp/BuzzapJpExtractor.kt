package com.jocmp.mercury.extractors.custom.buzzap.jp

import com.jocmp.mercury.extractors.extractor

val BuzzapJpExtractor =
    extractor("buzzap.jp") {
        title { selectors("h1.entry-title") }

        datePublished { attr("time.entry-date", "datetime") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.ctiframe")

            defaultCleaner = false
        }
    }
