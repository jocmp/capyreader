package com.jocmp.mercury.extractors.custom.www.huffingtonpost.com

import com.jocmp.mercury.extractors.extractor

val WwwHuffingtonpostComExtractor =
    extractor("www.huffingtonpost.com") {
        title { selectors("h1.headline__title") }

        author { selectors("span.author-card__details__name") }

        datePublished {
            attr("meta[name=\"article:modified_time\"]", "value")
            attr("meta[name=\"article:published_time\"]", "value")
        }

        dek { selectors("h2.headline__subtitle") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.entry__body")
            defaultCleaner = false
            clean(
                ".pull-quote",
                ".tag-cloud",
                ".embed-asset",
                ".below-entry",
                ".entry-corrections",
                "#suggested-story",
            )
        }
    }
