package com.jocmp.mercury.extractors.custom.getnews.jp

import com.jocmp.mercury.extractors.extractor

val GetnewsJpExtractor =
    extractor("getnews.jp") {
        title { selectors("article h1") }

        author {
            attr("meta[name=\"article:author\"]", "value")
            selector("span.prof")
        }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            attr("ul.cattag-top time", "datetime")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.post-bodycopy")
        }
    }
