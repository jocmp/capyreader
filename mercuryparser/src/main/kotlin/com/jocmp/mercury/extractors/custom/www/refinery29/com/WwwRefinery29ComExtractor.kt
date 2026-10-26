package com.jocmp.mercury.extractors.custom.www.refinery29.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwRefinery29ComExtractor =
    extractor("www.refinery29.com") {
        title { selectors("h1.title") }

        author { selectors(".contributor") }

        datePublished {
            attr("meta[name=\"sailthru.date\"]", "value")
            timezone = "America/New_York"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            compound(".full-width-opener", ".article-content")
            selector(".article-content")
            selector(".body")

            transform("div.loading noscript") { node, _ ->
                val imgHtml = node.html()
                node.parent(".loading").replaceWith(imgHtml)
                TransformResult.NoChange
            }
            transform(".section-image", renameTo = "figure")
            transform(".section-image .content-caption", renameTo = "figcaption")
            transform(".section-text", renameTo = "p")

            clean(".story-share")
        }
    }
