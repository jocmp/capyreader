package com.jocmp.mercury.extractors.custom.www.littlethings.com

import com.jocmp.mercury.extractors.extractor

val LittleThingsExtractor =
    extractor("www.littlethings.com") {
        title { selectors("h1[class*=\"PostHeader\"]", "h1.post-title") }

        author {
            selector("div[class^=\"PostHeader__ScAuthorNameSection\"]")
            attr("meta[name=\"author\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(
                "section[class*=\"PostMainArticle\"]",
                ".mainContentIntro",
                ".content-wrapper",
            )
        }
    }
