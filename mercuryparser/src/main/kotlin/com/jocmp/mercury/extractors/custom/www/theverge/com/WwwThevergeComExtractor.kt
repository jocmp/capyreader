package com.jocmp.mercury.extractors.custom.www.theverge.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor
import org.jsoup.Jsoup

val WwwThevergeComExtractor =
    extractor("www.theverge.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".duet--layout--entry-body", "article")

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

            transform("img") { node, _ ->
                val srcset = node.attr("srcset")
                val src = (srcset ?: "").split(",").firstOrNull()

                if (!src.isNullOrEmpty()) {
                    val parent = node.elements.firstOrNull()?.parent()
                    if (parent?.parent() != null) {
                        val replacement =
                            Jsoup
                                .parseBodyFragment(
                                    "<figure><img srcset=\"$srcset\" src=\"$src\"/></figure>",
                                ).body()
                                .child(0)
                        parent.replaceWith(replacement)
                    }
                }
                TransformResult.NoChange
            }

            clean(
                ".duet--article--timestamp",
                "[id*=\"-article_footer\"]",
                "[id*=\"-article_footer\"] ~ *",
                "#comments",
                "#comments ~ *",
            )
        }
    }
