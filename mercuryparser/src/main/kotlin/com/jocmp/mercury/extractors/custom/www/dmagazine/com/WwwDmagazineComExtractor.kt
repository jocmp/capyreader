package com.jocmp.mercury.extractors.custom.www.dmagazine.com

import com.jocmp.mercury.extractors.extractor

val WwwDmagazineComExtractor =
    extractor("www.dmagazine.com") {
        title { selectors("h1.story__title") }

        author { selectors(".story__info .story__info__item:first-child") }

        datePublished {
            timezone = "America/Chicago"
            format = "MMMM D, YYYY h:mm a"
            selectors(".story__info")
        }

        dek { selectors(".story__subhead") }

        leadImageUrl { attr("article figure a:first-child", "href") }

        content {
            selectors(".story__content")
        }
    }
