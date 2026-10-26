package com.jocmp.mercury.extractors.custom.superseriale.se.pl

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val SuperserialeSePlExtractor =
    extractor("superseriale.se.pl") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { selectors(".article_author:first-of-type") }

        datePublished {
            timezone = "Europe/Warsaw"
            selectors("#timezone")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article")

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }

            clean(
                "#timezone",
                ".author",
                ".article__author__croppimg",
                ".related_articles__elements",
                ".gl_plugin.socials",
                ".gl_plugin.player",
                ".gl_plugin.video_player",
                ".gl_plugin + video",
            )
        }
    }
