package com.jocmp.mercury.extractors.custom.www.ossnews.jp

import com.jocmp.mercury.extractors.extractor

val WwwOssnewsJpExtractor =
    extractor("www.ossnews.jp") {
        title { selectors("#alpha-block h1.hxnewstitle") }

        datePublished { selectors("p.fs12") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("#alpha-block .section:has(h1.hxnewstitle)")
            defaultCleaner = false
        }
    }
