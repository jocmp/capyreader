@file:Suppress("ktlint:standard:package-name")

package com.jocmp.mercury.extractors.custom.www._1pezeshk.com

import com.jocmp.mercury.extractors.extractor

val Www1pezeshkComExtractor =
    extractor("www.1pezeshk.com") {
        title {
            attr("meta[name=\"og:title\"]", "value")
            selector("h1.post-title")
        }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr(".featured-area img", "src") }

        content {
            selectors("article > .entry-content")

            // Upstream defines an `img` transform `$node.src = decodeURIComponent($node.src)`,
            // but cheerio nodes don't expose `.src` as a property — the assignment is a
            // no-op in JS. Skipping the transform matches the JS behavior.
        }
    }
