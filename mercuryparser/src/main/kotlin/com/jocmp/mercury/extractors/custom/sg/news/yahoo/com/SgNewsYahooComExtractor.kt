package com.jocmp.mercury.extractors.custom.sg.news.yahoo.com

import com.jocmp.mercury.extractors.extractor

val SgNewsYahooComExtractor =
    extractor("sg.news.yahoo.com") {
        title {
            attr("meta[name=\"og:title\"]", "value")
            selector("title")
        }

        author {
            selector(".caas-attr-provider")
            selector("meta[name=\"author\"]")
        }

        datePublished {
            timezone = "UTC"
            selector("time[datetime]")
            selector("meta[property=\"article:published_time\"]")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".caas-body-content", "article")

            clean(
                ".caas-header",
                ".caas-logo",
                ".caas-title-wrapper",
                "button",
                ".advertisement",
                ".sda-*",
                "[data-content=\"Advertisement\"]",
            )
        }
    }
