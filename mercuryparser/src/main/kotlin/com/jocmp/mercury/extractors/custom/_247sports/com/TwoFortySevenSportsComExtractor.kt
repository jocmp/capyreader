@file:Suppress("ktlint:standard:package-name")

package com.jocmp.mercury.extractors.custom._247sports.com

import com.jocmp.mercury.extractors.extractor

val TwoFortySevenSportsComExtractor =
    extractor("247sports.com") {
        title { selectors("title", "article header h1") }

        author { selectors(".article-cnt__author", ".author") }

        datePublished { attr("time[data-published]", "data-published") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".article-body", "section.body.article")

            // Is there anything in the content you selected that needs transformed
            // before it's consumable content? E.g., unusual lazy loaded images

            // Is there anything that is in the result that shouldn't be?
            // The clean selectors will remove anything that matches from
            // the result
        }
    }
