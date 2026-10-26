package com.jocmp.mercury.extractors.custom.www.androidauthority.com

import com.jocmp.mercury.dsl.Selection
import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

private const val AFFILIATE_PREFIX = "Affiliate links on Android Authority may earn us a commission."

private fun removeAffiliateLink(node: Selection) {
    if (node.text().startsWith(AFFILIATE_PREFIX)) {
        node.remove()
    }
}

private fun removePolls(node: Selection): Boolean {
    val el = node.elements.firstOrNull() ?: return false
    val parent = el.parent() ?: return false
    val hasPollButton = parent.children().any { sib -> sib.`is`("button:not(:has(picture))") }
    if (hasPollButton) {
        parent.remove()
        return true
    }
    return false
}

val WwwAndroidauthorityComExtractor =
    extractor("www.androidauthority.com") {
        title {
            attr("meta[name=\"og:title\"]", "value")
            selector("h1")
        }

        author { selectors("button.d_ic") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("main")

            transform("div") { node, _ ->
                removeAffiliateLink(node)
                TransformResult.NoChange
            }
            transform("p") { node, _ ->
                if (node.text().startsWith("Published on")) {
                    node.remove()
                }
                removeAffiliateLink(node)
                TransformResult.NoChange
            }
            transform("ol") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("h3") { node, _ ->
                if (!removePolls(node)) {
                    node.attr("class", "mercury-parser-keep")
                }
                TransformResult.NoChange
            }

            clean("h1 + div", "picture + div")
        }
    }
