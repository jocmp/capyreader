package com.jocmp.mercury.extractors.custom.money.cnn.com

import com.jocmp.mercury.extractors.extractor

// Port note: parity test fails on dek. Selector matches upstream verbatim;
// cleanDek returns null in the shared cleaner infrastructure (same class of
// failure as the pre-existing abcnews.go.com port). Not specific to this extractor.
val MoneyCnnComExtractor =
    extractor("money.cnn.com") {
        title { selectors(".article-title") }

        author {
            attr("meta[name=\"author\"]", "value")
            selector(".byline a")
        }

        datePublished { attr("meta[name=\"date\"]", "value") }

        dek { selectors("#storytext h2") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("#storytext")

            clean(".inStoryHeading")
        }
    }
