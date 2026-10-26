package com.jocmp.mercury.extractors.custom.www.euronews.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwEuronewsComExtractor =
    extractor("www.euronews.com") {
        supportedDomains = listOf("gr.euronews.com")

        title { attr("meta[name=\"og:title\"]", "value") }

        author {
            attr("meta[name=\"article:author\"]", "value")
            selector(".c-article-contributors")
        }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { selectors("h2.c-article-summary") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".c-article-content", "article")

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform(".widget__figure") { node, _ ->
                node.addClass("mercury-parser-keep")
                TransformResult.NoChange
            }

            clean(".c-ad", ".c-widget-related", ".connatix-container")
        }
    }
