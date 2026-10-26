package com.jocmp.mercury.extractors.custom.wikipedia.org

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WikipediaOrgExtractor =
    extractor("wikipedia.org") {
        title { selectors("h2.title") }

        // Upstream sets `author: 'Wikipedia Contributors'` — a constant string
        // literal rather than a selector. The DSL doesn't model constant-valued
        // fields yet, so author falls through to the generic extractor here.

        datePublished { selectors("#footer-info-lastmod") }

        content {
            selectors("#mw-content-text")

            defaultCleaner = false

            // Transform top infobox to an image with caption.
            transform(".infobox img") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val infobox =
                    generateSequence(el.parent()) { it.parent() }
                        .firstOrNull { p -> p.classNames().contains("infobox") }
                if (infobox != null && infobox.children().none { it.tagName() == "img" }) {
                    infobox.prependChild(el)
                }
                TransformResult.NoChange
            }
            transform(".infobox caption", renameTo = "figcaption")
            transform(".infobox", renameTo = "figure")

            clean(
                ".mw-editsection",
                "figure tr, figure td, figure tbody",
                "#toc",
                ".navbox",
            )
        }
    }
