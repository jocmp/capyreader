package com.jocmp.mercury.extractors.custom.ma.ttias.be

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val MaTtiasBeExtractor =
    extractor("ma.ttias.be") {
        title { attr("meta[name=\"twitter:title\"]", "value") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        content {
            compound(".content")

            // Is there anything in the content you selected that needs transformed
            // before it's consumable content? E.g., unusual lazy loaded images
            transform("h2") { node, _ ->
                // The "id" attribute values would result in low scores and the element being
                // removed.
                node.removeAttr("id")

                // h1 elements will be demoted to h2, so demote h2 elements to h3.
                TransformResult.Rename("h3")
            }
            transform("h1") { node, _ ->
                // The "id" attribute values would result in low scores and the element being
                // removed.
                node.removeAttr("id")

                // A subsequent h2 will be removed if there is not a paragraph before it, so
                // add a paragraph here. It will be removed anyway because it is empty.
                node.after("<p></p>")
                TransformResult.NoChange
            }
            transform("ul") { node, _ ->
                // Articles contain lists of links which look like, but are not, navigation
                // elements. Adding this class attribute avoids them being incorrectly removed.
                node.attr("class", "entry-content-asset")
                TransformResult.NoChange
            }
        }
    }
