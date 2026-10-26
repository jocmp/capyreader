package com.jocmp.mercury.extractors.custom.fortune.com

import com.jocmp.mercury.extractors.extractor

val FortuneComExtractor =
    extractor("fortune.com") {
        title { selectors("h1") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished {
            selectors(".MblGHNMJ")
            timezone = "UTC"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            compound("picture", "article.row")
            selector("article.row")

            // Is there anything in the content you selected that needs transformed
            // before it's consumable content? E.g., unusual lazy loaded images

            // Is there anything that is in the result that shouldn't be?
            // The clean selectors will remove anything that matches from
            // the result
        }
    }
