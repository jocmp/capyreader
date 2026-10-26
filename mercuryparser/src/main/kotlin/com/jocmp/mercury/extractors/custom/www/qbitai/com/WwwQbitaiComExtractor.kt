package com.jocmp.mercury.extractors.custom.www.qbitai.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwQbitaiComExtractor =
    extractor("www.qbitai.com") {
        title { selectors("title", "h1") }

        content {
            selectors(".article")

            transform(".zhaiyao") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }

            clean(".article_info")
        }
    }
