package com.jocmp.mercury.extractors.custom.qz.com

import com.jocmp.mercury.extractors.extractor

val QzComExtractor =
    extractor("qz.com") {
        title { selectors("article header h1") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            attr("time[datetime]", "datetime")
        }

        leadImageUrl {
            attr("meta[name=\"og:image\"]", "value")
            attr("meta[property=\"og:image\"]", "content")
            attr("meta[name=\"twitter:image\"]", "content")
        }

        content {
            selectors("#article-content")
        }
    }
