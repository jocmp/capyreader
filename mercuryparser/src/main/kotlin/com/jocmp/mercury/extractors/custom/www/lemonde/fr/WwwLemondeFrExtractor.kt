package com.jocmp.mercury.extractors.custom.www.lemonde.fr

import com.jocmp.mercury.extractors.extractor

val WwwLemondeFrExtractor =
    extractor("www.lemonde.fr") {
        title { selectors("h1.article__title") }

        author { selectors(".author__name") }

        datePublished { attr("meta[name=\"og:article:published_time\"]", "value") }

        dek { selectors(".article__desc") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".article__content")
            clean("figcaption")
        }
    }
