package com.jocmp.mercury.extractors.custom.techlog.iij.ad.jp

import com.jocmp.mercury.extractors.extractor

val TechlogIijAdJpExtractor =
    extractor("techlog.iij.ad.jp") {
        title { selectors("h1.entry-title") }

        author { selectors("a[rel=\"author\"]") }

        datePublished { attr("time.entry-date", "datetime") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.entry-content")

            defaultCleaner = false

            clean(".wp_social_bookmarking_light")
        }
    }
