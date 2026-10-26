package com.jocmp.mercury.extractors.custom.epaper.zeit.de

import com.jocmp.mercury.extractors.extractor

val EpaperZeitDeExtractor =
    extractor("epaper.zeit.de") {
        title { selectors("p.title") }

        author { selectors(".article__author") }

        excerpt { selectors("subtitle") }

        content {
            selectors(".article")

            // Is there anything in the content you selected that needs transformed
            // before it's consumable content? E.g., unusual lazy loaded images
            transform("p.title", renameTo = "h1")
            transform(".article__author", renameTo = "p")
            transform("byline", renameTo = "p")
            transform("linkbox", renameTo = "p")

            // Is there anything that is in the result that shouldn't be?
            // The clean selectors will remove anything that matches from
            // the result
            clean("image-credits", "box[type=citation]")
        }
    }
