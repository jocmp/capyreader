@file:Suppress("ktlint:standard:package-name")

package com.jocmp.mercury.extractors.custom.ici.radio_canada.ca

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val IciRadioCanadaCaExtractor =
    extractor("ici.radio-canada.ca") {
        title { attr("meta[name=\"dc.title\"]", "value") }

        author { attr("meta[name=\"dc.creator\"]", "value") }

        datePublished { attr("meta[name=\"dc.date.created\"]", "value") }

        dek { attr("meta[name=\"description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article main", "article")
            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("ul") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            clean(
                "header",
                "nav",
                "button",
                "figcaption",
                "[class*=\"adBox\"]",
                ".framed",
            )
        }
    }
