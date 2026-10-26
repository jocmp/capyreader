@file:Suppress("ktlint:standard:package-name")

package com.jocmp.mercury.extractors.custom._9to5google.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val NineToFiveGoogleComExtractor =
    extractor("9to5google.com") {
        title { selectors("title", "h1") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("main")

            transform("img") { node, _ ->
                node.removeAttr("sizes")
                TransformResult.NoChange
            }

            clean(".post-meta")
        }
    }
