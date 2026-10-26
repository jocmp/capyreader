package com.jocmp.mercury.extractors.custom.www.gizmodo.jp

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwGizmodoJpExtractor =
    extractor("www.gizmodo.jp") {
        title { selectors("h1.p-post-title") }

        author { selectors("li.p-post-AssistAuthor") }

        datePublished { attr("li.p-post-AssistTime time", "datetime") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article.p-post")

            transform("img.p-post-thumbnailImage") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val src = el.attr("src")
                if (src.isNotEmpty()) {
                    val cleaned = src.replace(Regex("""^.*=%27"""), "").replace(Regex("""%27;$"""), "")
                    el.attr("src", cleaned)
                }
                TransformResult.NoChange
            }

            clean("h1.p-post-title", "ul.p-post-Assist")
        }
    }
