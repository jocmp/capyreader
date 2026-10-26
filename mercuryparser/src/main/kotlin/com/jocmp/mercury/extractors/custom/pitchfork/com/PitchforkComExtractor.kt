package com.jocmp.mercury.extractors.custom.pitchfork.com

import com.jocmp.mercury.extractors.extractor

val PitchforkComExtractor =
    extractor("pitchfork.com") {
        title {
            attr("meta[name=\"og:title\"]", "value")
            selector("title")
        }

        author {
            attr("meta[name=\"article:author\"]", "value")
            selector(".authors-detail__display-name")
        }

        datePublished {
            selector("div[class^=\"InfoSliceWrapper-\"]")
            attr(".pub-date", "datetime")
        }

        dek {
            attr("meta[name=\"og:description\"]", "value")
            selector(".review-detail__abstract")
        }

        leadImageUrl {
            attr("meta[name=\"og:image\"]", "value")
            attr(".single-album-tombstone__art img", "src")
        }

        content {
            selectors("div.body__inner-container", ".review-detail__text")
        }

        // Upstream also defines an `extend` block for `score`. The DSL doesn't
        // model extended custom fields yet — the standard fields above are ported.
    }
