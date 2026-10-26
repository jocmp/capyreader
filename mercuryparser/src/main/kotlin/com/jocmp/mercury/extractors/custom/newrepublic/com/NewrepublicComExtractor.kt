package com.jocmp.mercury.extractors.custom.newrepublic.com

import com.jocmp.mercury.extractors.extractor

val NewrepublicComExtractor =
    extractor("newrepublic.com") {
        title { selectors("h1.article-headline") }

        author { selectors("span.AuthorList") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { selectors("h2.article-subhead") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            compound("div.article-body")

            clean("aside")
        }
    }
