package com.jocmp.mercury.extractors.custom.www.investmentexecutive.com

import com.jocmp.mercury.extractors.extractor

val WwwInvestmentexecutiveComExtractor =
    extractor("www.investmentexecutive.com") {
        title { selectors("h1") }

        author { selectors("div[itemprop=\"author\"]") }

        datePublished { attr("meta[itemprop=\"datePublished\"]", "value") }

        dek { attr("meta[name=\"og:description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("section.article-body")
            clean(".hidden")
        }
    }
