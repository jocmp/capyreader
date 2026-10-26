package com.jocmp.mercury.extractors.custom.gonintendo.com

import com.jocmp.mercury.extractors.extractor

val GonintendoComExtractor =
    extractor("gonintendo.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        datePublished { attr("meta[name=\"og:article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".content")

            clean(".text-brand-gray-600")
        }
    }
