package com.jocmp.mercury.extractors.custom.www.al.com

import com.jocmp.mercury.extractors.extractor

val WwwAlComExtractor =
    extractor("www.al.com") {
        title { attr("meta[name=\"title\"]", "value") }

        author { attr("meta[name=\"article_author\"]", "value") }

        datePublished {
            timezone = "EST"
            attr("meta[name=\"article_date_original\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".entry-content")
        }
    }
