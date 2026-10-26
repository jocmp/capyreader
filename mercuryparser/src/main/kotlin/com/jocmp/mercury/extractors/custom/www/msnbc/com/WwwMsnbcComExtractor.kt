package com.jocmp.mercury.extractors.custom.www.msnbc.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwMsnbcComExtractor =
    extractor("www.msnbc.com") {
        title { selectors("h1", "h1.is-title-pane") }

        author { selectors(".byline-name", ".author") }

        datePublished {
            attr("meta[itemprop=\"datePublished\"]", "value")
            attr("meta[name=\"DC.date.issued\"]", "value")
        }

        dek { attr("meta[name=\"description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".article-body__content", ".pane-node-body")

            transform(".pane-node-body") { node, doc ->
                val src = doc("meta[name=\"og:image\"]").attr("value")
                if (!src.isNullOrEmpty()) {
                    node.elements.firstOrNull()?.prepend("<img src=\"$src\" />")
                }
                TransformResult.NoChange
            }
        }
    }
