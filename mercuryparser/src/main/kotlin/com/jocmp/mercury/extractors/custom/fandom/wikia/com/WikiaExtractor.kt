package com.jocmp.mercury.extractors.custom.fandom.wikia.com

import com.jocmp.mercury.extractors.extractor

// Rename CustomExtractor
// to fit your publication
// (e.g., NYTimesExtractor)
val WikiaExtractor =
    extractor("fandom.wikia.com") {
        title {
            selectors("h1.entry-title")
        }

        author {
            selectors(".author vcard", ".fn")
        }

        content {
            selectors(".grid-content", ".entry-content")

            // Is there anything in the content you selected that needs transformed
            // before it's consumable content? E.g., unusual lazy loaded images

            // Is there anything that is in the result that shouldn't be?
            // The clean selectors will remove anything that matches from
            // the result
        }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }
    }
