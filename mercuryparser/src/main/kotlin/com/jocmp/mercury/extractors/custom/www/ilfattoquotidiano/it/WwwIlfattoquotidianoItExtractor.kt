package com.jocmp.mercury.extractors.custom.www.ilfattoquotidiano.it

import com.jocmp.mercury.extractors.extractor

val WwwIlfattoquotidianoItExtractor =
    extractor("www.ilfattoquotidiano.it") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author {
            selectors(".ifq-post__author .ifq-news-meta__author-name")
            clean("span")
        }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { attr("meta[name=\"og:description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".ifq-post__content", "article")
        }
    }
