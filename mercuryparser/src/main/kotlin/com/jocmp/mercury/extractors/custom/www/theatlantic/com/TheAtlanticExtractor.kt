package com.jocmp.mercury.extractors.custom.www.theatlantic.com

import com.jocmp.mercury.extractors.extractor

// Rename CustomExtractor
// to fit your publication
val TheAtlanticExtractor =
    extractor("www.theatlantic.com") {
        title { selectors("h1", ".c-article-header__hed") }

        author {
            attr("meta[name=\"author\"]", "value")
            selector(".c-byline__author")
        }

        content {
            selectors("article", ".article-body")

            // Is there anything in the content you selected that needs transformed
            // before it's consumable content? E.g., unusual lazy loaded images

            // Is there anything that is in the result that shouldn't be?
            // The clean selectors will remove anything that matches from
            // the result
            clean(
                ".partner-box",
                ".callout",
                ".c-article-writer__image",
                ".c-article-writer__content",
                ".c-letters-cta__text",
                ".c-footer__logo",
                ".c-recirculation-link",
                ".twitter-tweet",
            )
        }

        dek { attr("meta[name=\"description\"]", "value") }

        datePublished { attr("time[itemprop=\"datePublished\"]", "datetime") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }
    }
