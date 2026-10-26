package com.jocmp.mercury.extractors.custom.www.today.com

import com.jocmp.mercury.extractors.extractor

val WwwTodayComExtractor =
    extractor("www.today.com") {
        title { selectors("h1.article-hero-headline__htag", "h1.entry-headline") }

        author {
            selector("span.byline-name")
            attr("meta[name=\"author\"]", "value")
        }

        datePublished {
            selector("time[datetime]")
            attr("meta[name=\"DC.date.issued\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.article-body__content", ".entry-container")
            clean(".label-comment")
        }
    }
