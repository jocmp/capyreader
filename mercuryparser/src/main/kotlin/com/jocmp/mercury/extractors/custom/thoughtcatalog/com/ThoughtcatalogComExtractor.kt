package com.jocmp.mercury.extractors.custom.thoughtcatalog.com

import com.jocmp.mercury.extractors.extractor

val ThoughtcatalogComExtractor =
    extractor("thoughtcatalog.com") {
        title {
            selector("h1.title")
            attr("meta[name=\"og:title\"]", "value")
        }

        author {
            selector("cite a")
            selector(
                "div.col-xs-12.article_header div.writer-container.writer-container-inline." +
                    "writer-no-avatar h4.writer-name",
            )
            selector("h1.writer-name")
        }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".entry.post")

            clean(".tc_mark", "figcaption")
        }
    }
