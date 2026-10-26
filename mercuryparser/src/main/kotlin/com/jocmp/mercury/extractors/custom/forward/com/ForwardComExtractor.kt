package com.jocmp.mercury.extractors.custom.forward.com

import com.jocmp.mercury.extractors.extractor

val ForwardComExtractor =
    extractor("forward.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author {
            selector(".post-author a")
            selector(".author-name")
            attr("meta[name=\"sailthru.author\"]", "value")
        }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            attr("meta[name=\"date\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selector(".content-container article")
            compound(".post-item-media-wrap", ".post-item p")

            // Is there anything in the content you selected that needs transformed
            // before it's consumable content? E.g., unusual lazy loaded images

            // Is there anything that is in the result that shouldn't be?
            // The clean selectors will remove anything that matches from
            // the result
            clean(".post-author", ".donate-box", ".message", ".subtitle")
        }
    }
