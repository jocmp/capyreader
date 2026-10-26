package com.jocmp.mercury.extractors.custom.japan.zdnet.com

import com.jocmp.mercury.extractors.extractor

val JapanZdnetComExtractor =
    extractor("japan.zdnet.com") {
        title { selectors("h1") }

        author { attr("meta[name=\"cXenseParse:author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content { selectors("div.article_body") }
    }
