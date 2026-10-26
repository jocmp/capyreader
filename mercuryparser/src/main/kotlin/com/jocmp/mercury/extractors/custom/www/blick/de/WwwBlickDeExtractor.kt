package com.jocmp.mercury.extractors.custom.www.blick.de

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwBlickDeExtractor =
    extractor("www.blick.de") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { selectors(".article-meta__author") }

        datePublished { attr("time.article-meta__date[datetime]", "datetime") }

        dek { attr("meta[name=\"og:description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article")

            defaultCleaner = false

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("figcaption details") { node, _ ->
                val text = node.text()
                node.replaceWith("<span>$text</span>")
                TransformResult.NoChange
            }
            transform("ul.gallery__item-wrapper", renameTo = "div")
            transform("li.gallery__item", renameTo = "div")

            clean(
                ".section-header",
                ".article__footer",
                ".social-button-container",
                ".gallery__button",
                ".gallery__position-label",
                ".detail-img__caption-toggle",
                ".nativendo-mid-article",
                ".taboola-mid-article",
                "article > p",
            )
        }
    }
