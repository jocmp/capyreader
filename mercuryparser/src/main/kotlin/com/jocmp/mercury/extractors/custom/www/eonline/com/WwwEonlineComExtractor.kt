package com.jocmp.mercury.extractors.custom.www.eonline.com

import com.jocmp.mercury.extractors.extractor

val WwwEonlineComExtractor =
    extractor("www.eonline.com") {
        title { selectors("h1.article-detail__title", "h1.article__title") }

        author { selectors(".article-detail__meta__author", ".entry-meta__author a") }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            attr("meta[itemprop=\"datePublished\"]", "value")
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            compound(".article-detail__main-content section")
            compound(".post-content section, .post-content div.post-content__image")

            transform("div.post-content__image", renameTo = "figure")
            transform("div.post-content__image .image__credits", renameTo = "figcaption")
        }
    }
