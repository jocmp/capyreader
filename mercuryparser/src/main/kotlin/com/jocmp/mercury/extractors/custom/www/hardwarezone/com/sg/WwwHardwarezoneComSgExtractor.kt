package com.jocmp.mercury.extractors.custom.www.hardwarezone.com.sg

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwHardwarezoneComSgExtractor =
    extractor("www.hardwarezone.com.sg") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { selectors(".article-view-author-name a") }

        datePublished {
            timezone = "UTC"
            selectors(".article-view-timestamp")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".content", "article")

            transform("img") { node, _ ->
                node.removeAttr("sizes")
                TransformResult.NoChange
            }
            transform("p") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
        }
    }
