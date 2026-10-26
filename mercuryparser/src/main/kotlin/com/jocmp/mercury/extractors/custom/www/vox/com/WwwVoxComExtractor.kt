package com.jocmp.mercury.extractors.custom.www.vox.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwVoxComExtractor =
    extractor("www.vox.com") {
        title { selectors("h1.c-page-title") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { selectors(".p-dek") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            compound("figure.e-image--hero", ".c-entry-content")
            selector(".c-entry-content")

            transform("figure .e-image__image noscript") { node, _ ->
                val imgHtml = node.html()
                node.parent(".e-image__image").find(".c-dynamic-image").replaceWith(imgHtml)
                TransformResult.NoChange
            }
            transform("figure .e-image__meta", renameTo = "figcaption")
        }
    }
