package com.jocmp.mercury.extractors.custom.uproxx.com

import com.jocmp.mercury.extractors.extractor

val UproxxComExtractor =
    extractor("uproxx.com") {
        title { selectors("div.entry-header h1") }

        author { attr("meta[name=\"qc:author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".entry-content")

            transform("div.image", renameTo = "figure")
            transform("div.image .wp-media-credit", renameTo = "figcaption")
        }
    }
