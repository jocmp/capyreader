package com.jocmp.mercury.extractors.custom.www.nydailynews.com

import com.jocmp.mercury.extractors.extractor

val WwwNydailynewsComExtractor =
    extractor("www.nydailynews.com") {
        title { selectors("h1.headline", "h1#ra-headline") }

        author {
            selector(".article_byline span")
            attr("meta[name=\"parsely-author\"]", "value")
        }

        datePublished {
            selector("time")
            attr("meta[name=\"sailthru.date\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article", "article#ra-body")
            clean("dl#ra-tags", ".ra-related", "a.ra-editor", "dl#ra-share-bottom")
        }
    }
