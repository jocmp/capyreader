package com.jocmp.mercury.extractors.custom.techcrunch.com

import com.jocmp.mercury.extractors.extractor

val TechcrunchComExtractor =
    extractor("techcrunch.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("main")

            clean("img.post-authors-list__author-thumb")
        }
    }
