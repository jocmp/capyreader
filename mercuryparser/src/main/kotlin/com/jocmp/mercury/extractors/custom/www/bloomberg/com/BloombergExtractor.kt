package com.jocmp.mercury.extractors.custom.www.bloomberg.com

import com.jocmp.mercury.extractors.extractor

val BloombergExtractor =
    extractor("www.bloomberg.com") {
        title {
            selectors(
                // normal articles
                ".lede-headline",
                // /graphics/ template
                "h1.article-title",
                // /news/ template
                "h1[class^=\"headline\"]",
                "h1.lede-text-only__hed",
            )
        }

        author {
            attr("meta[name=\"parsely-author\"]", "value")
            selector(".byline-details__link")
            // /graphics/ template
            selector(".bydek")
            // /news/ template
            selector(".author")
            selector("p[class*=\"author\"]")
        }

        datePublished {
            attr("time.published-at", "datetime")
            attr("time[datetime]", "datetime")
            attr("meta[name=\"date\"]", "value")
            attr("meta[name=\"parsely-pub-date\"]", "value")
            attr("meta[name=\"parsely-pub-date\"]", "content")
        }

        leadImageUrl {
            attr("meta[name=\"og:image\"]", "value")
            attr("meta[name=\"og:image\"]", "content")
        }

        content {
            selectors(
                ".article-body__content",
                ".body-content",
                // /graphics/ template
                "section.copy-block",
                // /news/ template
                ".body-copy",
            )

            clean(".inline-newsletter", ".page-ad")
        }
    }
