package com.jocmp.mercury.extractors.custom.gothamist.com

import com.jocmp.mercury.extractors.extractor

val GothamistComExtractor =
    extractor("gothamist.com") {
        supportedDomains =
            listOf(
                "chicagoist.com",
                "laist.com",
                "sfist.com",
                "shanghaiist.com",
                "dcist.com",
            )

        title { selectors("h1", ".entry-header h1") }

        author {
            // There are multiple article-metadata and byline-author classes, but the main article's is the 3rd child of the l-container class
            selectors(".article-metadata:nth-child(3) .byline-author", ".author")
        }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            selector("abbr")
            selector("abbr.published")
            timezone = "America/New_York"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".article-body", ".entry-body")

            // Is there anything in the content you selected that needs transformed
            // before it's consumable content? E.g., unusual lazy loaded images
            transform("div.image-none", renameTo = "figure")
            transform(".image-none i", renameTo = "figcaption")
            transform("div.image-left", renameTo = "figure")
            transform(".image-left i", renameTo = "figcaption")
            transform("div.image-right", renameTo = "figure")
            transform(".image-right i", renameTo = "figcaption")

            // Is there anything that is in the result that shouldn't be?
            // The clean selectors will remove anything that matches from
            // the result
            clean(
                ".image-none br",
                ".image-left br",
                ".image-right br",
                ".galleryEase",
            )
        }
    }
