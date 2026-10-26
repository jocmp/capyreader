package com.jocmp.mercury.extractors.custom.www.rollingstone.com

import com.jocmp.mercury.extractors.extractor

val WwwRollingstoneComExtractor =
    extractor("www.rollingstone.com") {
        title { selectors("h1.l-article-header__row--title", "h1.content-title") }

        author { selectors("a.c-byline__link", "a.content-author.tracked-offpage") }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            selector("time.content-published-date")
            timezone = "America/New_York"
        }

        dek { selectors("h2.l-article-header__row--lead", ".content-description") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selector(".l-article-content")
            compound(".lead-container", ".article-content")
            selector(".article-content")
            clean(".c-related-links-wrapper", ".module-related")
        }
    }
