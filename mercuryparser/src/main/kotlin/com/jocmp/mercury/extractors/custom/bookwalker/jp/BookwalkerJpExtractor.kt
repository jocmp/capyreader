package com.jocmp.mercury.extractors.custom.bookwalker.jp

import com.jocmp.mercury.extractors.extractor

val BookwalkerJpExtractor =
    extractor("bookwalker.jp") {
        title { selectors("h1.p-main__title", "h1.main-heading") }

        author { selectors("div.p-author__list", "div.authors") }

        datePublished {
            selectors(
                "dl.p-information__data dd:nth-of-type(7)",
                ".work-info .work-detail:first-of-type .work-detail-contents:last-of-type",
            )
            timezone = "Asia/Tokyo"
            // yet plumbed through the DSL → cleanDatePublished, so this date may
            // parse with a different offset than JS Mercury would produce.
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selector("div.p-main__information")
            compound("div.main-info", "div.main-cover-inner")

            defaultCleaner = false

            clean(
                "span.label.label--trial",
                "dt.info-head.info-head--coin",
                "dd.info-contents.info-contents--coin",
                "div.info-notice.fn-toggleClass",
            )
        }
    }
