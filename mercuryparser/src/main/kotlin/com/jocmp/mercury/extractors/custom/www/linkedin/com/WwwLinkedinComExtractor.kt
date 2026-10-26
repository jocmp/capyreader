package com.jocmp.mercury.extractors.custom.www.linkedin.com

import com.jocmp.mercury.extractors.extractor

val WwwLinkedinComExtractor =
    extractor("www.linkedin.com") {
        title { selectors(".article-title", "h1") }

        author {
            selector(".main-author-card h3")
            attr("meta[name=\"article:author\"]", "value")
            selector(".entity-name a[rel=author]")
        }

        datePublished {
            selectors(".base-main-card__metadata")
            attr("time[itemprop=\"datePublished\"]", "datetime")
            timezone = "America/Los_Angeles"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selector(".article-content__body")
            compound("header figure", ".prose")
            selector(".prose")
            clean(".entity-image")
        }
    }
