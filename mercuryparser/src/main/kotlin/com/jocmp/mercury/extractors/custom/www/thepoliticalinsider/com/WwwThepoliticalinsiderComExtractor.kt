package com.jocmp.mercury.extractors.custom.www.thepoliticalinsider.com

import com.jocmp.mercury.extractors.extractor

val WwwThepoliticalinsiderComExtractor =
    extractor("www.thepoliticalinsider.com") {
        title { attr("meta[name=\"sailthru.title\"]", "value") }

        author { attr("meta[name=\"sailthru.author\"]", "value") }

        datePublished {
            attr("meta[name=\"sailthru.date\"]", "value")
            timezone = "America/New_York"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div#article-body")
        }
    }
