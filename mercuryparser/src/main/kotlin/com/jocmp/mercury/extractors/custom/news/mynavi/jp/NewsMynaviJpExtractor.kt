package com.jocmp.mercury.extractors.custom.news.mynavi.jp

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val NewsMynaviJpExtractor =
    extractor("news.mynavi.jp") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author {
            selector("a.articleHeader_name")
            selector("main div.article-author a.article-author__name")
        }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { attr("meta[name=\"og:description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.article-body", "main article div")

            transform("img") { node, _ ->
                val src = node.attr("data-original")
                if (!src.isNullOrEmpty()) {
                    node.attr("src", src)
                }
                TransformResult.NoChange
            }
        }
    }
