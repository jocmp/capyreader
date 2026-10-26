package com.jocmp.mercury.extractors.custom.www.polygon.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwPolygonComExtractor =
    extractor("www.polygon.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article")

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("h3") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("img") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val srcset = el.attr("srcset")
                val src = (srcset.ifEmpty { "" }).split(",").firstOrNull()?.trim().orEmpty()
                if (src.isNotEmpty()) {
                    val parent = el.parent() ?: return@transform TransformResult.NoChange
                    if (parent.parent() == null) return@transform TransformResult.NoChange
                    val figure =
                        org.jsoup.parser.Parser
                            .parseBodyFragment(
                                "<figure><img srcset=\"$srcset\" src=\"$src\"/></figure>",
                                parent.ownerDocument()?.baseUri() ?: "",
                            ).body()
                            .child(0)
                    parent.replaceWith(figure)
                }
                TransformResult.NoChange
            }

            clean(
                "cite",
                ".duet--ad--native-ad-rail",
                ".duet--layout--rail",
                ".duet--article--table-of-contents",
            )
        }
    }
