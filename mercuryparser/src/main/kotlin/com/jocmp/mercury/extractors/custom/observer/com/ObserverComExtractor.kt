package com.jocmp.mercury.extractors.custom.observer.com

import com.jocmp.mercury.extractors.extractor

val ObserverComExtractor =
    extractor("observer.com") {
        title { selectors("h1.entry-title") }

        author { selectors(".author", ".vcard") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { selectors("h2.dek") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.entry-content")
        }
    }
