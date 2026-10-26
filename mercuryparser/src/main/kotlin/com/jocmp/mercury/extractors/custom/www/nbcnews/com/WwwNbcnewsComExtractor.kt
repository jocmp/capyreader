package com.jocmp.mercury.extractors.custom.www.nbcnews.com

import com.jocmp.mercury.extractors.extractor

val WwwNbcnewsComExtractor =
    extractor("www.nbcnews.com") {
        title { selectors("div.article-hero-headline h1", "div.article-hed h1") }

        author {
            selectors(
                "div.article-inline-byline span.byline-name",
                "span.byline_author",
            )
        }

        datePublished {
            attr("meta[name=\"article:published\"]", "value")
            attr(".flag_article-wrapper time.timestamp_article[datetime]", "datetime")
            selector(".flag_article-wrapper time")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.article-body__content", "div.article-body")
        }
    }
