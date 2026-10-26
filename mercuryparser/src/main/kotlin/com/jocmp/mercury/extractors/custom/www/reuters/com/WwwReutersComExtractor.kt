package com.jocmp.mercury.extractors.custom.www.reuters.com

import com.jocmp.mercury.extractors.extractor

val WwwReutersComExtractor =
    extractor("www.reuters.com") {
        title { selectors("h1[class*=\"ArticleHeader-headline-\"]", "h1.article-headline") }

        author {
            attr("meta[name=\"og:article:author\"]", "value")
            selector(".author")
        }

        datePublished { attr("meta[name=\"og:article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.ArticleBodyWrapper", "#article-text")

            transform(".article-subtitle", renameTo = "h4")

            clean(
                "div[class^=\"ArticleBody-byline-container-\"]",
                "#article-byline .author",
            )
        }
    }
