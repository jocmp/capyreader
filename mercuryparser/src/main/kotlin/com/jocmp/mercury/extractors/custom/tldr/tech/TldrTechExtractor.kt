package com.jocmp.mercury.extractors.custom.tldr.tech

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val TldrTechExtractor =
    extractor("tldr.tech") {
        title { selectors("h1") }

        leadImageUrl { attr("meta[name=\"twitter:image\"]", "value") }

        content {
            selectors(".content-center", "body")

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("h3") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
        }
    }
