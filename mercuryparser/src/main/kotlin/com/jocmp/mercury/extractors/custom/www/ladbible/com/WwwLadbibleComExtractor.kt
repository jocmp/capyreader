package com.jocmp.mercury.extractors.custom.www.ladbible.com

import com.jocmp.mercury.extractors.extractor

val WwwLadbibleComExtractor =
    extractor("www.ladbible.com") {
        title { selectors("h1") }

        author { selectors("[class*=Byline]") }

        datePublished {
            selectors("time")
            timezone = "Europe/London"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("[class*=ArticleContainer]")
            clean(
                "time",
                "source",
                "a[href^=\"https://www.ladbible.com/\"]",
                "picture",
                "[class*=StyledCardBlock]",
            )
        }
    }
