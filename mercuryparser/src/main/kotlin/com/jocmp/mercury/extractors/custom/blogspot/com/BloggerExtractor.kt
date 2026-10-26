package com.jocmp.mercury.extractors.custom.blogspot.com

import com.jocmp.mercury.extractors.extractor

val BloggerExtractor =
    extractor("blogspot.com") {
        content {
            // Blogger is insane and does not load its content
            // initially in the page, but it's all there
            // in noscript
            selectors(".post-content noscript")

            // Selectors to remove from the extracted content

            // Convert the noscript tag to a div
            transform("noscript", renameTo = "div")
        }

        author { selectors(".post-author-name") }

        title { selectors(".post h2.title") }

        datePublished { selectors("span.publishdate") }
    }
