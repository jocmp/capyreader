package com.jocmp.mercury.extractors.custom.www.rawstory.com

import com.jocmp.mercury.extractors.extractor

val WwwRawstoryComExtractor =
    extractor("www.rawstory.com") {
        title {
            attr("meta[name=\"og:title\"]", "value")
            selector(".blog-title")
        }

        author {
            selectors(
                "div.main-post-head .social-author__name",
                ".blog-author a:first-of-type",
            )
        }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            selector(".blog-author a:last-of-type")
            timezone = "EST"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".post-body", ".blog-content")
        }
    }
