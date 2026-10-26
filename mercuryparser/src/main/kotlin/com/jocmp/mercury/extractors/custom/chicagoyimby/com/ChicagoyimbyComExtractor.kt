package com.jocmp.mercury.extractors.custom.chicagoyimby.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val ChicagoyimbyComExtractor =
    extractor("chicagoyimby.com") {
        title { selectors("h1.post-title") }

        author { selectors(".entry-meta-author a") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".entry-content")

            transform("img") { node, _ ->
                node.removeAttr("sizes")
                TransformResult.NoChange
            }

            clean(".breadcrumb")
        }
    }
