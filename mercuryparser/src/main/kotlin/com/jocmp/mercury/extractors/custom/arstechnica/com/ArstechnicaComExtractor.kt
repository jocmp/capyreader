package com.jocmp.mercury.extractors.custom.arstechnica.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val ArstechnicaComExtractor =
    extractor("arstechnica.com") {
        title { selectors("title", "h1") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".post-content", "main")

            transform("img") { node, _ ->
                node.removeAttr("width")
                node.removeAttr("sizes")
                TransformResult.NoChange
            }

            clean("header", ".upper-deck__text", ".text-settings-dropdown-story")
        }
    }
