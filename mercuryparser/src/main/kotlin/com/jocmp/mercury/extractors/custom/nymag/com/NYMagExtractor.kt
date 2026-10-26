package com.jocmp.mercury.extractors.custom.nymag.com

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val NYMagExtractor =
    extractor("nymag.com") {
        content {
            // Order by most likely. Extractor will stop on first occurrence
            selectors("div.article-content", "section.body", "article.article")

            // Selectors to remove from the extracted content
            clean(".ad", ".single-related-story")

            // Object of tranformations to make on matched elements
            // Each key is the selector, each value is the tag to
            // transform to.
            // If a function is given, it should return a string
            // to convert to or nothing (in which case it will not perform
            // the transformation.

            // Convert h1s to h2s
            transform("h1", renameTo = "h2")

            // Convert lazy-loaded noscript images to figures
            transform("noscript") { node, _ ->
                val noscriptHtml = node.html()
                if (noscriptHtml.isEmpty()) return@transform TransformResult.NoChange
                val parsed = Doc.load(noscriptHtml, isDocument = false)
                val children = parsed("*")
                if (children.length == 1 && children.first().elements.first()?.tagName()?.lowercase() == "img") {
                    TransformResult.Rename("figure")
                } else {
                    TransformResult.NoChange
                }
            }
        }

        title { selectors("h1.lede-feature-title", "h1.headline-primary", "h1") }
        author { selectors(".by-authors", ".lede-feature-author") }
        dek { selectors(".lede-feature-teaser") }
        datePublished {
            attr("time.article-timestamp[datetime]", "datetime")
            selector("time.article-timestamp")
        }
    }
