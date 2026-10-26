package com.jocmp.mercury.extractors.custom.www.msn.com

import com.jocmp.mercury.extractors.extractor

val MSNExtractor =
    extractor("www.msn.com") {
        title { selectors("h1") }

        author { selectors("span.authorname-txt") }

        datePublished { selectors("span.time") }

        content {
            selectors("div.richtext")
            clean("span.caption")
        }
    }
