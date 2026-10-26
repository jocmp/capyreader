package com.jocmp.mercury.extractors.custom.www.gruene.de

import com.jocmp.mercury.extractors.extractor

val WwwGrueneDeExtractor =
    extractor("www.gruene.de") {
        title { selectors("header h1") }

        leadImageUrl { attr("meta[property=\"og:image\"]", "content") }

        content {
            compound("section header", "section h2", "section p", "section ol")

            clean("figcaption", "p[class]")
        }
    }
