package com.jocmp.mercury.extractors.custom.pagesix.com

import com.jocmp.mercury.extractors.extractor

val PagesixComExtractor =
    extractor("pagesix.com") {
        supportedDomains = listOf("nypost.com")

        title { attr("meta[name=\"og:title\"]", "value") }

        author { selectors(".byline") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { attr("meta[name=\"description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            compound("#featured-image-wrapper", ".entry-content")
            selector(".entry-content")

            transform("#featured-image-wrapper", renameTo = "figure")
            transform(".wp-caption-text", renameTo = "figcaption")

            clean(".modal-trigger")
        }
    }
