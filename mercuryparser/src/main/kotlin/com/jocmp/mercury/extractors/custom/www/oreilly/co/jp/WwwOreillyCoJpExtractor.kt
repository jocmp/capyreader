package com.jocmp.mercury.extractors.custom.www.oreilly.co.jp

import com.jocmp.mercury.extractors.extractor

val WwwOreillyCoJpExtractor =
    extractor("www.oreilly.co.jp") {
        title {
            attr("meta[name=\"og:title\"]", "value")
            selector("h3")
        }

        author { selectors("span[itemprop=\"author\"]", "li[itemprop=\"author\"]") }

        datePublished {
            attr("dd[itemprop=\"datePublished\"]", "content")
            attr("meta[itemprop=\"datePublished\"]", "value")
        }

        leadImageUrl {
            attr("meta[name=\"og:image:secure_url\"]", "value")
            attr("meta[name=\"og:image\"]", "value")
        }

        content {
            selectors("section.detail", "#content")
            defaultCleaner = false
            clean(".social-tools")
        }
    }
