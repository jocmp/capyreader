package com.jocmp.mercury.extractors.custom.abcnews.go.com

import com.jocmp.mercury.extractors.extractor

val AbcnewsGoComExtractor =
    extractor("abcnews.go.com") {
        title { selectors("div[class*=\"Article_main__body\"] h1", ".article-header h1") }

        author {
            selectors(".ShareByline span:nth-child(2)", ".authors")
            clean(".author-overlay", ".by-text")
        }

        datePublished {
            selectors(".ShareByline", ".timestamp")
            timezone = "America/New_York"
            format = "MMMM D, YYYY h:mm a"
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("article", ".article-copy")

            // Is there anything in the content you selected that needs transformed
            // before it's consumable content? E.g., unusual lazy loaded images

            // Is there anything that is in the result that shouldn't be?
            // The clean selectors will remove anything that matches from
            // the result
        }
    }
