package com.jocmp.mercury.extractors.custom.terminaltrove.com

import com.jocmp.mercury.extractors.extractor

val TerminaltroveComExtractor =
    extractor("terminaltrove.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        dek { attr("meta[name=\"og:description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("main")

            clean(
                ".share-badge",
                ".modal",
                ".modal-toggle",
                ".sr-only",
                ".premium-sponsor-featured",
            )
        }
    }
