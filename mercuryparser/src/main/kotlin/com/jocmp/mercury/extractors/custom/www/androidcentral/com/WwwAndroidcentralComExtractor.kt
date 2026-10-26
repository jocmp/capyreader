package com.jocmp.mercury.extractors.custom.www.androidcentral.com

import com.jocmp.mercury.extractors.extractor

val WwwAndroidcentralComExtractor =
    extractor("www.androidcentral.com") {
        title { selectors("h1", "h1.main-title") }

        author { attr("meta[name=\"parsely-author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { attr("meta[name=\"description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("#article-body")

            clean(".intro", "blockquote")
        }
    }
