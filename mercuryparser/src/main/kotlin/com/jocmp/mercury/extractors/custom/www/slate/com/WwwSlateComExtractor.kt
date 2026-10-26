package com.jocmp.mercury.extractors.custom.www.slate.com

import com.jocmp.mercury.extractors.extractor

val WwwSlateComExtractor =
    extractor("www.slate.com") {
        title { selectors(".hed", "h1") }

        author { selectors("a[rel=author]") }

        datePublished {
            selectors(".pub-date")
            timezone = "America/New_York"
        }

        dek { selectors(".dek") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".body")
            clean(
                ".about-the-author",
                ".pullquote",
                ".newsletter-signup-component",
                ".top-comment",
            )
        }
    }
