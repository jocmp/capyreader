@file:Suppress("ktlint:standard:package-name")

package com.jocmp.mercury.extractors.custom.www.futura_sciences.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwFuturaSciencesComExtractor =
    extractor("www.futura-sciences.com") {
        title { selectors("title", "h1") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("#article-anchor-article-main-content", ".article-text")

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("h3") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("h4") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("ul") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }

            clean(".cWHWfD", "span[class*=\"wrappers__Span\"]")
        }
    }
