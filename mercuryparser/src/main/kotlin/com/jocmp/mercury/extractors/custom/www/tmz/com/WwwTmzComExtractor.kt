package com.jocmp.mercury.extractors.custom.www.tmz.com

import com.jocmp.mercury.extractors.extractor

val WwwTmzComExtractor =
    extractor("www.tmz.com") {
        title { selectors(".post-title-breadcrumb", "h1", ".headline") }

        author { literal("TMZ STAFF") }

        datePublished {
            selectors(".article__published-at", ".article-posted-date")
            timezone = "America/Los_Angeles"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".article__blocks", ".article-content", ".all-post-body")
            clean(".lightbox-link")
        }
    }
