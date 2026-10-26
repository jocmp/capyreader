package com.jocmp.mercury.extractors.custom.www.chicagotribune.com

import com.jocmp.mercury.extractors.extractor

val WwwChicagotribuneComExtractor =
    extractor("www.chicagotribune.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { selectors("div.article_byline span:first-of-type") }

        datePublished { selectors("time") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article")
        }
    }
