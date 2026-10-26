package com.jocmp.mercury.extractors.custom.www.americanow.com

import com.jocmp.mercury.extractors.extractor

val WwwAmericanowComExtractor =
    extractor("www.americanow.com") {
        title {
            selector(".title")
            attr("meta[name=\"title\"]", "value")
        }

        author { selectors(".byline") }

        datePublished { attr("meta[name=\"publish_date\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            compound(".article-content", ".image", ".body")
            selector(".body")

            clean(".article-video-wrapper", ".show-for-small-only")
        }
    }
