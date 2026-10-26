package com.jocmp.mercury.extractors.custom.www.cnbc.com

import com.jocmp.mercury.extractors.extractor

val WwwCnbcComExtractor =
    extractor("www.cnbc.com") {
        title { selectors("h1.title", "h1.ArticleHeader-headline") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(
                "div#article_body.content",
                "div.story",
                "div.ArticleBody-articleBody",
            )
        }
    }
