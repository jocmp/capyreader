package com.jocmp.mercury.extractors.custom.www.bustle.com

import com.jocmp.mercury.extractors.extractor

val WwwBustleComExtractor =
    extractor("www.bustle.com") {
        title { selectors("h1", "h1.post-page__title") }

        author { selectors("a[href*=\"profile\"]", "div.content-meta__author") }

        datePublished { attr("time", "datetime") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article", ".post-page__body")
        }
    }
