package com.jocmp.mercury.extractors.custom.www.politico.com

import com.jocmp.mercury.extractors.extractor

val PoliticoExtractor =
    extractor("www.politico.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author {
            attr("div[itemprop=\"author\"] meta[itemprop=\"name\"]", "value")
            selector(".story-meta__authors .vcard")
            selector(".story-main-content .byline .vcard")
        }

        datePublished {
            attr("time[itemprop=\"datePublished\"]", "datetime")
            attr(".story-meta__details time[datetime]", "datetime")
            attr(".story-main-content .timestamp time[datetime]", "datetime")
            timezone = "America/New_York"
        }

        dek { attr("meta[name=\"og:description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            compound(".story-text")
            selector(".story-main-content")
            selector(".story-core")
            clean("figcaption", ".story-meta", ".ad")
        }
    }
