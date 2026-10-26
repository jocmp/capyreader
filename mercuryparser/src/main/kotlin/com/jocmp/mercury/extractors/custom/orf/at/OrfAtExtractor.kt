package com.jocmp.mercury.extractors.custom.orf.at

import com.jocmp.mercury.extractors.extractor

val OrfAtExtractor =
    extractor("orf.at") {
        title { selectors("title") }

        datePublished { attr("meta[name=\"dc.date\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("main")

            clean(".story-meta")
        }
    }
