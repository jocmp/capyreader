package com.jocmp.mercury.extractors.custom.www.newyorker.com

import com.jocmp.mercury.extractors.extractor

val NewYorkerExtractor =
    extractor("www.newyorker.com") {
        title {
            selectors(
                "h1[class^=\"content-header\"]",
                "h1[class^=\"ArticleHeader__hed\"]",
                "h1[class*=\"ContentHeaderHed\"]",
            )
            attr("meta[name=\"og:title\"]", "value")
        }

        author {
            selector("article header div[class^=\"BylinesWrapper\"]")
            attr("meta[name=\"article:author\"]", "value")
            selector("div[class^=\"ArticleContributors\"] a[rel=\"author\"]")
            selector("article header div[class*=\"Byline__multipleContributors\"]")
        }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            selector("time.content-header__publish-date")
            attr("meta[name=\"pubdate\"]", "value")
            timezone = "America/New_York"
        }

        dek {
            selectors(
                "div[class^=\"ContentHeaderDek\"]",
                "div.content-header__dek",
                "h2[class^=\"ArticleHeader__dek\"]",
            )
            attr("meta[name=\"description\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(
                ".article__body",
                "article.article.main-content",
                "main[class^=\"Layout__content\"]",
            )

            transform(".caption__text", renameTo = "figcaption")
            transform(".caption__credit", renameTo = "figcaption")

            clean("footer[class^=\"ArticleFooter__footer\"]", "aside")
        }
    }
