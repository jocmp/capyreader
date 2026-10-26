package com.jocmp.mercury.extractors.custom.www.mentalfloss.com

import com.jocmp.mercury.extractors.extractor

val WwwMentalflossComExtractor =
    extractor("www.mentalfloss.com") {
        title {
            attr("meta[name=\"og:title\"]", "value")
            selector("h1.title")
            selector(".title-group")
            selector(".inner")
        }

        author {
            selectors(
                "a[data-vars-label*=\"authors\"]",
                ".field-name-field-enhanced-authors",
            )
        }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            selector(".date-display-single")
            timezone = "America/New_York"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article main", "div.field.field-name-body")
            clean("small")
        }
    }
