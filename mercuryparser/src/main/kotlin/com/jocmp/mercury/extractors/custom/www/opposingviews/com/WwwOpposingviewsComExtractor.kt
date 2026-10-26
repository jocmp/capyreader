package com.jocmp.mercury.extractors.custom.www.opposingviews.com

import com.jocmp.mercury.extractors.extractor

val WwwOpposingviewsComExtractor =
    extractor("www.opposingviews.com") {
        title { selectors("h1.m-detail-header--title", "h1.title") }

        author {
            attr("meta[name=\"author\"]", "value")
            selector("div.date span span a")
        }

        datePublished {
            attr("meta[name=\"published\"]", "value")
            attr("meta[name=\"publish_date\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".m-detail--body", ".article-content")
            clean(".show-for-small-only")
        }
    }
