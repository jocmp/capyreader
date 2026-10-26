package com.jocmp.mercury.extractors.custom.sciencefly.com

import com.jocmp.mercury.extractors.extractor

val ScienceflyComExtractor =
    extractor("sciencefly.com") {
        title { selectors(".entry-title", ".cb-entry-title", ".cb-single-title") }

        author { selectors("div.cb-author", "div.cb-author-title") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("div.theiaPostSlider_slides img", "src") }

        content {
            selectors("div.theiaPostSlider_slides")
        }
    }
