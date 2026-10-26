package com.jocmp.mercury.extractors.custom.www.spektrum.de

import com.jocmp.mercury.extractors.extractor

val SpektrumExtractor =
    extractor("www.spektrum.de") {
        title { selectors(".content__title") }

        author { selectors(".content__author__info__name") }

        datePublished {
            selectors(".content__meta__date")
            timezone = "Europe/Berlin"
            format = "D.M.YYYY"
        }

        dek { selectors(".content__intro") }

        leadImageUrl {
            attr("meta[name=\"og:image\"]", "value")
            attr("meta[property=\"og:image\"]", "content")
            selector(".image__article__top img")
        }

        content {
            selectors("article.content")
            clean(
                ".breadcrumbs",
                ".hide-for-print",
                "aside",
                "header h2",
                ".image__article__top",
                ".content__author",
                ".copyright",
                ".callout-box",
            )
        }
    }
