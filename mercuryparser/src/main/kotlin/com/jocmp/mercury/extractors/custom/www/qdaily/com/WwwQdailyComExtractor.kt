package com.jocmp.mercury.extractors.custom.www.qdaily.com

import com.jocmp.mercury.extractors.extractor

val WwwQdailyComExtractor =
    extractor("www.qdaily.com") {
        title { selectors("h2", "h2.title") }

        author { selectors(".name") }

        datePublished { attr(".date.smart-date", "data-origindate") }

        dek { selectors(".excerpt") }

        leadImageUrl { attr(".article-detail-hd img", "src") }

        content {
            selectors(".detail")
            clean(".lazyload", ".lazylad", ".lazylood")
        }
    }
