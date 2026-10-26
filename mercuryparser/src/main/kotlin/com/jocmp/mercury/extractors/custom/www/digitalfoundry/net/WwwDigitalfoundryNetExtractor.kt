package com.jocmp.mercury.extractors.custom.www.digitalfoundry.net

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwDigitalfoundryNetExtractor =
    extractor("www.digitalfoundry.net") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".article-text", "article")

            transform("iframe[data-src]") { node, _ ->
                val dataSrc = node.attr("data-src") ?: return@transform TransformResult.NoChange
                node.attr("src", dataSrc)
                TransformResult.NoChange
            }

            clean(".youtube-sub", ".object-related", ".poll", ".insert", ".see-also")
        }
    }
