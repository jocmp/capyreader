package com.jocmp.mercury.extractors.custom.news.nationalgeographic.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val NewsNationalgeographicComExtractor =
    extractor("news.nationalgeographic.com") {
        title { selectors("h1", "h1.main-title") }

        author { selectors(".byline-component__contributors b span") }

        datePublished {
            timezone = "EST"
            format = "ddd MMM D HH:mm:ss zz YYYY"
            attr("meta[name=\"article:published_time\"]", "value")
        }

        dek { selectors(".article__deck") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            compound(".parsys.content", ".__image-lead__")
            selector(".content")

            transform(".parsys.content") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val imgSrc = el.selectFirst(".image.parbase.section .picturefill")?.attr("data-platform-src")
                if (!imgSrc.isNullOrEmpty()) {
                    el.prepend("<img class=\"__image-lead__\" src=\"$imgSrc\"/>")
                }
                TransformResult.NoChange
            }

            clean(".pull-quote.pull-quote--large")
        }
    }
