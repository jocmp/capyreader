package com.jocmp.mercury.extractors.custom.www.lifehacker.jp

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

private val LEADING_LAZY = Regex("^.*=%27")
private val TRAILING_LAZY = Regex("%27;$")

val WwwLifehackerJpExtractor =
    extractor("www.lifehacker.jp") {
        title { selectors("h1[class^=\"article_pArticle_Title\"]", "h1.lh-summary-title") }

        author {
            attr("meta[name=\"author\"]", "value")
            selector("p.lh-entryDetailInner--credit")
        }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            attr("div.lh-entryDetail-header time", "datetime")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(
                "div[class^=\"article_pArticle_Body__\"]",
                "div.lh-entryDetail-body",
            )

            transform("img.lazyload") { node, _ ->
                val src = node.attr("src") ?: return@transform TransformResult.NoChange
                val cleaned = src.replace(LEADING_LAZY, "").replace(TRAILING_LAZY, "")
                node.attr("src", cleaned)
                TransformResult.NoChange
            }

            clean("p.lh-entryDetailInner--credit")
        }
    }
