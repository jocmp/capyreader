package com.jocmp.mercury.extractors.custom.www.wired.com

import com.jocmp.mercury.extractors.extractor

// Rename CustomExtractor
// to fit your publication
// (e.g., NYTimesExtractor)
val WiredExtractor =
    extractor("www.wired.com") {
        title {
            selectors(
                "h1[data-testId=\"ContentHeaderHed\"]",
                // enter title selectors
            )
        }

        author {
            attr("meta[name=\"article:author\"]", "value")
            selector("a[rel=\"author\"]")
            // enter author selectors
        }

        content {
            selectors(
                "article.article.main-content",
                "article.content",
                // enter content selectors
            )

            // Is there anything in the content you selected that needs transformed
            // before it's consumable content? E.g., unusual lazy loaded images

            // Is there anything that is in the result that shouldn't be?
            // The clean selectors will remove anything that matches from
            // the result
            clean(".visually-hidden", "figcaption img.photo", ".alert-message")
        }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }
    }
