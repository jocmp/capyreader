package com.jocmp.mercury.extractors.custom.www.vortez.net

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwVortezNetExtractor =
    extractor("www.vortez.net") {
        title { selectors("title") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        nextPageUrl { selectors(".pagelink:nth-child(2) > a") }

        content {
            selectors(".main-content", ".the-article-content")

            transform("strong", renameTo = "p")
            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }

            clean(".article-header", ".panel-title", "select", "br")
        }
    }
