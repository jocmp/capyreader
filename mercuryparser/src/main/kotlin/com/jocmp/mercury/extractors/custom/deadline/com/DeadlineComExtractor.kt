package com.jocmp.mercury.extractors.custom.deadline.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val DeadlineComExtractor =
    extractor("deadline.com") {
        title { selectors("h1") }

        author { selectors("section.author h2") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.a-article-grid__main.pmc-a-grid article.pmc-a-grid-item")

            transform(".embed-twitter") { node, _ ->
                val innerHtml = node.html()
                node.replaceWith(innerHtml)
                TransformResult.NoChange
            }

            clean("figcaption")
        }
    }
