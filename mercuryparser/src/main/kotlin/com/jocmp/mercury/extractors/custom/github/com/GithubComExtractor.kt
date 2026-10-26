package com.jocmp.mercury.extractors.custom.github.com

import com.jocmp.mercury.extractors.extractor

val GithubComExtractor =
    extractor("github.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        datePublished {
            attr("relative-time[datetime]", "datetime")
            attr("span[itemprop=\"dateModified\"] relative-time", "datetime")
        }

        dek {
            attr("meta[name=\"description\"]", "value")
            selector("span[itemprop=\"about\"]")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            // Upstream: [['#readme article']] (single-element array → treated as
            // the same as a scalar selector).
            selectors("#readme article")

            // Is there anything in the content you selected that needs transformed
            // before it's consumable content? E.g., unusual lazy loaded images

            // Is there anything that is in the result that shouldn't be?
            // The clean selectors will remove anything that matches from
            // the result
        }
    }
