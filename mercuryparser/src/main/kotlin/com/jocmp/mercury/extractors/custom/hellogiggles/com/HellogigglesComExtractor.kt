package com.jocmp.mercury.extractors.custom.hellogiggles.com

import com.jocmp.mercury.extractors.extractor

val HellogigglesComExtractor =
    extractor("hellogiggles.com") {
        title {
            attr("meta[name=\"og:title\"]", "value")
            selector(".title")
        }

        author { selectors(".byline-wrapper span.author_name", ".author-link") }

        datePublished {
            attr("meta[property=\"article:published_time\"]", "content")
            attr("meta[name=\"article:published_time\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".main-content", ".entry-content")
        }
    }
