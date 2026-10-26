package com.jocmp.mercury.extractors.custom.mashable.com

import com.jocmp.mercury.extractors.extractor

val MashableComExtractor =
    extractor("mashable.com") {
        title { selectors("header h1", "h1.title") }

        author {
            attr("meta[name=\"article:author\"]", "value")
            selector("span.author_name a")
        }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("#article", "section.article-content.blueprint")

            transform(".image-credit", renameTo = "figcaption")
        }
    }
