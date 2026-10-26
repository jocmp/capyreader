package com.jocmp.mercury.extractors.custom.www.versants.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwVersantsComExtractor =
    extractor("www.versants.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".article-content")

            transform(".featured-image") { node, _ ->
                node.addClass("mercury-parser-keep")
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val figure = el.selectFirst("figure")
                if (figure != null) {
                    el.select("span").forEach { figure.appendChild(it) }
                }
                TransformResult.NoChange
            }

            clean(
                ".adv-link",
                ".versa-target",
                "header",
                ".author",
                ".thumbnail-slider",
            )
        }
    }
