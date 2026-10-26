package com.jocmp.mercury.extractors.custom.www.tagesschau.de

import com.jocmp.mercury.extractors.extractor

val WwwTagesschauDeExtractor =
    extractor("www.tagesschau.de") {
        title { selectors(".seitenkopf__headline--text", "title") }

        author { selectors(".authorline__author authorline__link:first-child") }

        datePublished {
            attr("meta[name=\"date\"]", "value")
            selector(".metatextline")
            timezone = "UTC"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article")
            clean(
                "[data-config]",
                ".seitenkopf__headline",
                ".authorline__author",
                ".metatextline",
            )
        }
    }
