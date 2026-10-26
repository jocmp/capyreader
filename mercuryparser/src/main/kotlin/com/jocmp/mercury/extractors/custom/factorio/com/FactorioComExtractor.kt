package com.jocmp.mercury.extractors.custom.factorio.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val FactorioComExtractor =
    extractor("factorio.com") {
        title { selectors("title") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            compound(".blog-post", "div:nth-child(2)")

            transform("h3") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val author = el.selectFirst("author")
                val text = author?.text().orEmpty()
                if (text.isNotEmpty()) {
                    el.after("<p>$text</p>")
                    author?.remove()
                }
                TransformResult.NoChange
            }

            clean(".logo-expansion-space-age")
        }
    }
