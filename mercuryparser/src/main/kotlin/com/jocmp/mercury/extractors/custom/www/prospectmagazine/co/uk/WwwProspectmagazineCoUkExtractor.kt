package com.jocmp.mercury.extractors.custom.www.prospectmagazine.co.uk

import com.jocmp.mercury.extractors.extractor

val WwwProspectmagazineCoUkExtractor =
    extractor("www.prospectmagazine.co.uk") {
        title { selectors(".blog-header__title", ".page-title") }

        author { selectors(".blog-header__author-link", ".aside_author .title") }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            selector(".post-info")
            timezone = "Europe/London"
        }

        dek { selectors(".blog-header__description", ".page-subtitle") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".blog__container", "article .post_content")
        }
    }
