package com.jocmp.mercury.extractors.custom.www.transfermarkt.de

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwTransfermarktDeExtractor =
    extractor("www.transfermarkt.de") {
        title { attr("meta[name=\"og:title\"]", "value") }

        datePublished {
            selectors(".news-header span:first-child")
            timezone = "Europe/Berlin"
            format = "DD.MM.YYYY - HH:mm"
        }

        dek { attr("meta[name=\"og:description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".news-content")
            defaultCleaner = false

            transform("h2") { node, _ ->
                node.attr("class", "mercury-parser-keep")
                TransformResult.NoChange
            }

            clean(
                ".dachzeile",
                ".news-header-social",
                ".newsansicht-bildquelle",
                ".news-widget--container",
                ".pinpoll",
                ".advertisment-button-container",
                "tm-consent",
            )
        }
    }
