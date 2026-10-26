package com.jocmp.mercury.extractors.custom.otrs.com

import com.jocmp.mercury.extractors.extractor

val OtrsComExtractor =
    extractor("otrs.com") {
        title { selectors("#main article h1") }

        author { selectors("div.dateplusauthor a") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { attr("meta[name=\"og:description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("#main article")

            defaultCleaner = false

            clean(
                "div.dateplusauthor",
                "div.gr-12.push-6.footershare",
                "#atftbx",
                "div.category-modul",
            )
        }
    }
