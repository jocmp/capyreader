package com.jocmp.mercury.extractors.custom.www.engadget.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwEngadgetComExtractor =
    extractor("www.engadget.com") {
        title { selectors("title", "h1") }

        author { selectors(".caas-attr-item-author") }

        datePublished { attr("time", "datetime") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".caas-body")

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("blockquote noscript") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                if (el.selectFirst("iframe") != null) {
                    TransformResult.Rename("div")
                } else {
                    TransformResult.NoChange
                }
            }
        }
    }
