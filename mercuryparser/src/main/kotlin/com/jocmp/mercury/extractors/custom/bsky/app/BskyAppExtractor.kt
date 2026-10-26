package com.jocmp.mercury.extractors.custom.bsky.app

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val BskyAppExtractor =
    extractor("bsky.app") {
        title { attr("meta[name=\"og:title\"]", "value") }

        leadImageUrl {
            attr("meta[property=\"og:image\"]", "content")
            attr("meta[name=\"og:image\"]", "value")
        }

        content {
            selectors("noscript")

            transform("noscript") { node, _ ->
                val innerHtml = node.html()
                val parsed = Doc.load(innerHtml, isDocument = false)
                val summary = parsed("#bsky_post_text")
                node.replaceWith(summary.html())
                TransformResult.NoChange
            }
        }
    }
