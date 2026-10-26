@file:Suppress("ktlint:standard:package-name")

package com.jocmp.mercury.extractors.custom.takagi_hiromitsu.jp

import com.jocmp.mercury.extractors.extractor

val TakagiHiromitsuJpExtractor =
    extractor("takagi-hiromitsu.jp") {
        title { selectors("h3") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[http-equiv=\"Last-Modified\"]", "value") }

        content {
            selectors("div.body")

            defaultCleaner = false
        }
    }
