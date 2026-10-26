package com.jocmp.mercury.extractors.custom.www.fastcompany.com

import com.jocmp.mercury.extractors.extractor

val WwwFastcompanyComExtractor =
    extractor("www.fastcompany.com") {
        title { selectors("h1") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { selectors(".post__deck") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".post__article")
        }
    }
