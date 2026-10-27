package com.jocmp.mercury.extractors.custom.www.rosa.gr

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

val WwwRosaGrExtractor =
    extractor("www.rosa.gr") {
        title {
            attr("meta[name=\"og:title\"]", "value")
            selector("#hero h1")
        }

        author {
            selectors("#hero .author.show-desktop .author-name")
            attr("meta[name=\"author\"]", "value")
        }

        datePublished { attr("meta[name=\"article:published_time\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            compound("#hero .post-image", ".post-text")
            selectors(".post-text")

            // The hero image is a direct child; related-post thumbnails are wrapped in links.
            transform(".post-image:has(> img)", renameTo = "figure")
            transform(".image-credits") { node, _ ->
                // Credits are sometimes a bare source label such as "X".
                if (node.text().trim().length <= 2) {
                    node.remove()
                    TransformResult.NoChange
                } else {
                    TransformResult.Rename("figcaption")
                }
            }

            clean(
                ".short-read-also",
                ".post-list",
            )
        }
    }
