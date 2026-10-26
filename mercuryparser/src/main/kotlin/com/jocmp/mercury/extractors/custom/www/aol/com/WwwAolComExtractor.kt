package com.jocmp.mercury.extractors.custom.www.aol.com

import com.jocmp.mercury.extractors.extractor

val WwwAolComExtractor =
    extractor("www.aol.com") {
        title { selectors("h1.p-article__title") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished {
            timezone = "America/New_York"
            selectors(".p-article__byline__date")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".article-content")
        }
    }
