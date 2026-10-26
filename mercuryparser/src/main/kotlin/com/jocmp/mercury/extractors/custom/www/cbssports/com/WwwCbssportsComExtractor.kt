package com.jocmp.mercury.extractors.custom.www.cbssports.com

import com.jocmp.mercury.extractors.extractor

val WwwCbssportsComExtractor =
    extractor("www.cbssports.com") {
        title { selectors(".Article-headline", ".article-headline") }

        author { selectors(".ArticleAuthor-nameText", ".author-name") }

        datePublished {
            timezone = "UTC"
            attr("meta[itemprop=\"datePublished\"]", "value")
        }

        dek { selectors(".Article-subline", ".article-subline") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".article")
        }
    }
