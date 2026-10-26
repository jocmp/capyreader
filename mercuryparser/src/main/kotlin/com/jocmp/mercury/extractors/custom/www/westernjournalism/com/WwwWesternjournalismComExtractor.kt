package com.jocmp.mercury.extractors.custom.www.westernjournalism.com

import com.jocmp.mercury.extractors.extractor

val WwwWesternjournalismComExtractor =
    extractor("www.westernjournalism.com") {
        title { selectors("title", "h1.entry-title") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"DC.date.issued\"]", "value") }

        dek { selectors(".subtitle") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.article-sharing.top + div")
            clean(".ad-notice-small")
        }
    }
