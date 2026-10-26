package com.jocmp.mercury.extractors.custom.wired.jp

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor
import java.net.URI

// Port note: parity test fails on dek. Selector matches upstream verbatim;
// cleanDek returns null in the shared cleaner infrastructure (same class of
// failure as the pre-existing abcnews.go.com port). Not specific to this extractor.
val WiredJpExtractor =
    extractor("wired.jp") {
        title { selectors("h1[data-testid=\"ContentHeaderHed\"]", "h1.post-title") }

        author {
            attr("meta[name=\"article:author\"]", "value")
            selector("p[itemprop=\"author\"]")
        }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            attr("time", "datetime")
        }

        dek { selectors("div[class^=\"ContentHeaderDek\"]", ".post-intro") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(
                "div[data-attribute-verso-pattern=\"article-body\"]",
                "article.article-detail",
            )

            transform("img[data-original]") { node, _ ->
                val dataOriginal = node.attr("data-original")
                val src = node.attr("src")
                val url = URI.create(src ?: "").resolve(dataOriginal ?: "").toString()
                node.attr("src", url)
                TransformResult.NoChange
            }

            clean(".post-category", "time", "h1.post-title", ".social-area-syncer")
        }
    }
