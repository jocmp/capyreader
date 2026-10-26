package com.jocmp.mercury.extractors.custom.www.fool.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwFoolComExtractor =
    extractor("www.fool.com") {
        title { selectors("h1") }

        author {
            attr("meta[name=\"author\"]", "value")
            selector(".author-inline .author-name")
        }

        datePublished { attr("meta[name=\"date\"]", "value") }

        dek {
            attr("meta[name=\"og:description\"]", "value")
            selector("header h2")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".tailwind-article-body", ".article-content")

            transform(".caption img") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val src = el.attr("src")
                val parent = el.parent() ?: return@transform TransformResult.NoChange
                if (parent.parent() == null) return@transform TransformResult.NoChange
                parent.replaceWith(
                    org.jsoup.parser.Parser
                        .parseBodyFragment("<figure><img src=\"$src\"/></figure>", parent.ownerDocument()?.baseUri() ?: "")
                        .body()
                        .child(0),
                )
                TransformResult.NoChange
            }
            transform(".caption", renameTo = "figcaption")

            clean("#pitch")
        }
    }
