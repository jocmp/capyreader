package com.jocmp.mercury.extractors.custom.thefederalistpapers.org

import com.jocmp.mercury.extractors.extractor

val ThefederalistpapersOrgExtractor =
    extractor("thefederalistpapers.org") {
        title { selectors("h1.entry-title") }

        author { selectors(".author-meta-title", "main span.entry-author-name") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".content")

            // Upstream's last clean entry is wrapped as a 1-element array
            // `['p[style]']`; treated as the scalar selector here.
            clean(
                "header",
                ".article-sharing",
                ".after-article",
                ".type-commenting",
                ".more-posts",
                "p[style]",
            )
        }
    }
