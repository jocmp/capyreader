package com.jocmp.mercury.extractors.custom.www.broadwayworld.com

import com.jocmp.mercury.extractors.extractor

val BroadwayWorldExtractor =
    extractor("www.broadwayworld.com") {
        title { selectors("h1[itemprop=headline]", "h1.article-title") }

        author { selectors("span[itemprop=author]") }

        datePublished { attr("meta[itemprop=datePublished]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div[itemprop=articlebody]")
        }
    }
