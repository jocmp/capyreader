package com.jocmp.mercury.extractors.custom.polskisamorzad.se.pl

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val PolskisamorzadSePlExtractor =
    extractor("polskisamorzad.se.pl") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author {
            selector(".article_author:first-of-type")
            selector(".article-author")
            attr("meta[name=\"og:article:author\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".article-single")

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }

            clean(
                "#timezone",
                ".author",
                ".article__author__croppimg",
                ".article_authors_with_thumbnail",
                ".related_articles__elements",
                ".gl_plugin.socials",
                ".gl_plugin.player",
                ".gl_plugin.video_player",
                ".gl_plugin + video",
            )
        }
    }
