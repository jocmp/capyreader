package com.jocmp.mercury.extractors.custom.www.notebookcheck.net

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwNotebookcheckNetExtractor =
    extractor("www.notebookcheck.net") {
        title { selectors("h1") }

        author { selectors(".intro-author a") }

        datePublished {
            attr(".intro-author time", "datetime")
            timezone = "GMT"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("#content")

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("h3") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }
            transform("h4") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }

            clean(".ttcl_3", ".socialarea", ".tx-nbc2fe-relatedarticles", "aside")
        }
    }
