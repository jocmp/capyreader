package com.jocmp.mercury.extractors.custom.www.se.pl

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwSePlExtractor =
    extractor("www.se.pl") {
        // Upstream's bialystok.se.pl / lodz.se.pl / lublin.se.pl / polityka.se.pl /
        // portalobronny.se.pl / sport.se.pl / superbiz.se.pl / szczecin.se.pl /
        // wroclaw.se.pl all do `{...WwwSePlExtractor, domain: '<host>'}`. We
        // collapse them into supportedDomains since they share every selector.
        supportedDomains =
            listOf(
                "bialystok.se.pl",
                "lodz.se.pl",
                "lublin.se.pl",
                "polityka.se.pl",
                "portalobronny.se.pl",
                "sport.se.pl",
                "superbiz.se.pl",
                "szczecin.se.pl",
                "wroclaw.se.pl",
            )

        title { attr("meta[name=\"og:title\"]", "value") }

        author { selectors(".article_author:first-of-type") }

        datePublished {
            selectors("#timezone")
            timezone = "Europe/Warsaw"
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
                ".article_authors_with_thumbnail",
                ".related_articles__elements",
                ".gl_plugin.socials",
                ".gl_plugin.player",
                ".gl_plugin.video_player",
                ".gl_plugin + video",
            )
        }
    }
