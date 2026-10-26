@file:Suppress("ktlint:standard:package-name")

package com.jocmp.mercury.extractors.custom.www.n_tv.de

import com.jocmp.mercury.extractors.extractor

val WwwNtvDeExtractor =
    extractor("www.n-tv.de") {
        title { attr("meta[name=\"og:title\"]", "value") }

        datePublished { attr("meta[name=\"date\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".article__text", "article")
            clean(".article__share-main")
        }
    }
