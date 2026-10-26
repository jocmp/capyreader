package com.jocmp.mercury.extractors.custom.www.cbc.ca

import com.jocmp.mercury.extractors.extractor

val WwwCbcCaExtractor =
    extractor("www.cbc.ca") {
        title { selectors("h1") }

        author { selectors(".authorText", ".bylineDetails") }

        datePublished { attr(".timeStamp[datetime]", "datetime") }

        dek { selectors(".deck") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".story")
        }
    }
