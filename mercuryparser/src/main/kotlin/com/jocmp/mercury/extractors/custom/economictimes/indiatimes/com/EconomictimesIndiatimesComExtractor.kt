package com.jocmp.mercury.extractors.custom.economictimes.indiatimes.com

import com.jocmp.mercury.extractors.extractor

val EconomictimesIndiatimesComExtractor =
    extractor("economictimes.indiatimes.com") {
        title {
            selector("title")
            attr("meta[name=\"og:title\"]", "value")
        }

        author { selectors("a[rel=\"author\"]") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article")

            clean("span.imgAgency")
        }
    }
