package com.jocmp.mercury.extractors.custom.www.fortinet.com

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwFortinetComExtractor =
    extractor("www.fortinet.com") {
        title { selectors("h1") }

        author { selectors(".b15-blog-meta__author") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.responsivegrid.aem-GridColumn.aem-GridColumn--default--12")

            // Cheerio 1.x treats noscript content as text, not parsed HTML, so
            // we parse it manually to check if it's a single <img>.
            transform("noscript") { node, _ ->
                val noscriptHtml = node.html()
                if (noscriptHtml.isEmpty()) return@transform TransformResult.NoChange
                val parsed = Doc.load(noscriptHtml, isDocument = false)
                val children = parsed("*")
                if (children.length == 1 && children.first().elements.first()?.tagName()?.lowercase() == "img") {
                    TransformResult.Rename("figure")
                } else {
                    TransformResult.NoChange
                }
            }
        }
    }
