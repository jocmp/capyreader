package com.jocmp.mercury.extractors.custom.pastebin.com

import com.jocmp.mercury.extractors.extractor

val PastebinComExtractor =
    extractor("pastebin.com") {
        title { selectors("h1") }

        author { selectors(".username", ".paste_box_line2 .t_us + a") }

        datePublished {
            timezone = "America/New_York"
            format = "MMMM D, YYYY"
            selectors(".date", ".paste_box_line2 .t_da + span")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".source", "#selectable .text")

            transform("ol", renameTo = "div")
            transform("li", renameTo = "p")
        }
    }
