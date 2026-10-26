package com.jocmp.mercury.extractors.custom.www.videogameschronicle.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwVideogameschronicleComExtractor =
    extractor("www.videogameschronicle.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { selectors(".author-byline a[rel=\"author\"]") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        dek { attr("meta[name=\"og:description\"]", "value") }

        content {
            selectors(".post__content-body", "article")

            transform("figure a") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val href = el.attr("href")
                val img = el.selectFirst("img")
                if (href.isNotEmpty() && img != null && img.attr("src").isEmpty()) {
                    img.attr("src", href)
                    el.replaceWith(img)
                }
                TransformResult.NoChange
            }

            clean(".adcontainer")
        }
    }
