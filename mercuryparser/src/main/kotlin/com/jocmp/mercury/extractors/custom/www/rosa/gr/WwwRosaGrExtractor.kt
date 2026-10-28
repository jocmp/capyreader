package com.jocmp.mercury.extractors.custom.www.rosa.gr

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

private val PLATFORM_LABEL = Regex("^(X|Facebook|Instagram|TikTok|YouTube)$", RegexOption.IGNORE_CASE)

val WwwRosaGrExtractor =
    extractor("www.rosa.gr") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            compound("#hero .post-image", "#post .post-text")
            selectors("#post .post-text")

            // The hero image is a direct child; related-post thumbnails are wrapped in links.
            transform(".post-image:has(> img)", renameTo = "figure")
            transform(".post-image .image-credits") { node, _ ->
                // Embed credits are often just the source platform's name.
                if (PLATFORM_LABEL.matches(node.text().trim())) {
                    node.remove()
                    TransformResult.NoChange
                } else {
                    TransformResult.Rename("figcaption")
                }
            }

            clean(".short-read-also")
        }
    }
