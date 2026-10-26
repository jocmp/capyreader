package com.jocmp.mercury.extractors.custom.people.com

import com.jocmp.mercury.extractors.extractor

val PeopleComExtractor =
    extractor("people.com") {
        title {
            selector(".article-header h1")
            attr("meta[name=\"og:title\"]", "value")
        }

        author {
            attr("meta[name=\"sailthru.author\"]", "value")
            selector("a.author.url.fn")
        }

        datePublished {
            selector(".mntl-attribution__item-date")
            attr("meta[name=\"article:published_time\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        dek { selectors(".article-header h2") }

        content {
            selectors("div[class^=\"loc article-content\"]", "div.article-body__inner")
        }
    }
