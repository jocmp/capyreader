package com.jocmp.mercury.extractors.custom.www.inquisitr.com

import com.jocmp.mercury.extractors.extractor

val WwwInquisitrComExtractor =
    extractor("www.inquisitr.com") {
        title { selectors("h1.entry-title.story--header--title") }

        author { selectors("div.story--header--author") }

        datePublished { attr("meta[name=\"datePublished\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article.story", ".entry-content.")

            clean(
                ".post-category",
                ".story--header--socials",
                ".story--header--content",
            )
        }
    }
