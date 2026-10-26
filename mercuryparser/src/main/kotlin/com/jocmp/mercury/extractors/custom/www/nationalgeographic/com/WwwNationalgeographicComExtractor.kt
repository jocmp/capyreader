package com.jocmp.mercury.extractors.custom.www.nationalgeographic.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwNationalgeographicComExtractor =
    extractor("www.nationalgeographic.com") {
        title { selectors("h1", "h1.main-title") }

        author { selectors(".byline-component__contributors b span") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        dek { selectors(".Article__Headline__Desc", ".article__deck") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selector("section.Article__Content")
            compound(".parsys.content", ".__image-lead__")
            selector(".content")

            transform(".parsys.content") { node, _ ->
                val parent = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val imageParent = parent.children().firstOrNull()
                if (imageParent != null && imageParent.hasClass("imageGroup")) {
                    val dataAttrContainer =
                        imageParent
                            .select(".media--medium__container")
                            .firstOrNull()
                            ?.children()
                            ?.firstOrNull()
                    val imgPath1 = dataAttrContainer?.attr("data-platform-image1-path")
                    val imgPath2 = dataAttrContainer?.attr("data-platform-image2-path")
                    if (!imgPath1.isNullOrEmpty() && !imgPath2.isNullOrEmpty()) {
                        parent.prepend(
                            "<div class=\"__image-lead__\">\n" +
                                "                <img src=\"$imgPath1\"/>\n" +
                                "                <img src=\"$imgPath2\"/>\n" +
                                "              </div>",
                        )
                    }
                } else {
                    val imgSrc =
                        parent
                            .select(".image.parbase.section")
                            .select(".picturefill")
                            .firstOrNull()
                            ?.attr("data-platform-src")
                    if (!imgSrc.isNullOrEmpty()) {
                        parent.prepend("<img class=\"__image-lead__\" src=\"$imgSrc\"/>")
                    }
                }
                TransformResult.NoChange
            }

            clean(".pull-quote.pull-quote--small")
        }
    }
