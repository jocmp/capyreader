package com.jocmp.mercury.extractors.custom.scan.netsecurity.ne.jp

import com.jocmp.mercury.extractors.extractor

val ScanNetsecurityNeJpExtractor =
    extractor("scan.netsecurity.ne.jp") {
        title { selectors("header.arti-header h1.head") }

        datePublished { attr("meta[name=\"article:modified_time\"]", "value") }

        dek { selectors("header.arti-header p.arti-summary") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.arti-content.arti-content--thumbnail")

            defaultCleaner = false

            clean("aside.arti-giga")
        }
    }
