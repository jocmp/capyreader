package com.jocmp.mercury.extractors.custom.www.usmagazine.com

import com.jocmp.mercury.extractors.extractor

val WwwUsmagazineComExtractor =
    extractor("www.usmagazine.com") {
        title { selectors("header h1") }

        author { selectors("a.author", "a.article-byline.tracked-offpage") }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            timezone = "America/New_York"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.article-content")
            clean(".module-related")
        }
    }
