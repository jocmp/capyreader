package com.jocmp.mercury.extractors.custom.www.ndtv.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwNdtvComExtractor =
    extractor("www.ndtv.com") {
        title {
            attr("meta[name=\"og:title\"]", "value")
            selector("h1.entry-title")
        }

        author { selectors("span[itemprop=\"author\"] span[itemprop=\"name\"]") }

        datePublished { attr("span[itemprop=\"dateModified\"]", "content") }

        dek { selectors("h2") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div[itemprop=\"articleBody\"]")

            // Upstream moves an above-paragraph dateline `.place_cont` into the
            // first paragraph below it (only when it's not already inside a `<p>`).
            transform(".place_cont") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val insideP =
                    generateSequence(el.parent()) { it.parent() }.any { it.tagName() == "p" }
                if (!insideP) {
                    val sibling = el.nextElementSibling()
                    if (sibling != null && sibling.tagName() == "p") {
                        el.remove()
                        sibling.prependChild(el)
                    }
                }
                TransformResult.NoChange
            }

            clean(
                ".highlghts_Wdgt",
                ".ins_instory_dv_caption",
                "input",
                "._world-wrapper .mt20",
            )
        }
    }
