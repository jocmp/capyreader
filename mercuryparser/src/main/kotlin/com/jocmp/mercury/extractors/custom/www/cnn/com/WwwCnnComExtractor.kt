package com.jocmp.mercury.extractors.custom.www.cnn.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

// NOTE: upstream lists a multi-selector tuple `['.media__video--thumbnail', '.zn-body-text']`
// as the first content selector, which selects two regions and concatenates them. The
// Kotlin DSL's Selector data class only models single selectors / [selector, attribute]
// pairs, so that tuple is omitted here. The remaining selectors are ported verbatim.
val WwwCnnComExtractor =
    extractor("www.cnn.com") {
        title { selectors("h1.pg-headline", "h1") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(
                // a more specific selector to grab the lead image and the body
                // a fallback for the above
                ".zn-body-text",
                "div[itemprop=\"articleBody\"]",
            )

            // Is there anything in the content you selected that needs transformed
            // before it's consumable content? E.g., unusual lazy loaded images
            transform(".zn-body__paragraph, .el__leafmedia--sourced-paragraph") { node, _ ->
                val text = node.html()
                if (text.isNotEmpty()) {
                    TransformResult.Rename("p")
                } else {
                    TransformResult.NoChange
                }
            }

            // this transform cleans the short, all-link sections linking
            // to related content but not marked as such in any way.
            transform(".zn-body__paragraph") { node, _ ->
                if (node.find("a").length > 0) {
                    if (node.text().trim() == node.find("a").text().trim()) {
                        node.remove()
                    }
                }
                TransformResult.NoChange
            }

            transform(".media__video--thumbnail", renameTo = "figure")

            // Is there anything that is in the result that shouldn't be?
            // The clean selectors will remove anything that matches from
            // the result
        }
    }
