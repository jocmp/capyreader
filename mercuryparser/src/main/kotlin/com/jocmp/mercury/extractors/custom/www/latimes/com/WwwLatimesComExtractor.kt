package com.jocmp.mercury.extractors.custom.www.latimes.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwLatimesComExtractor =
    extractor("www.latimes.com") {
        title { selectors("h1.headline", ".trb_ar_hl") }

        author {
            selector("a[data-click=\"standardBylineAuthorName\"]")
            attr("meta[name=\"author\"]", "value")
        }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            attr("meta[itemprop=\"datePublished\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".page-article-body", ".trb_ar_main")

            transform(".trb_ar_la") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val figure = el.selectFirst("figure") ?: return@transform TransformResult.NoChange
                el.replaceWith(figure)
                TransformResult.NoChange
            }

            clean(".trb_ar_by", ".trb_ar_cr")
        }
    }
