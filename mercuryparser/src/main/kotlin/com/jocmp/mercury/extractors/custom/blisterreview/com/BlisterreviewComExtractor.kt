package com.jocmp.mercury.extractors.custom.blisterreview.com

import com.jocmp.mercury.extractors.extractor

val BlisterreviewComExtractor =
    extractor("blisterreview.com") {
        title {
            attr("meta[name=\"og:title\"]", "value")
            selector("h1.entry-title")
        }

        author { selectors("span.author-name") }

        datePublished {
            attr("meta[name=\"article:published_time\"]", "value")
            attr("time.entry-date", "datetime")
            attr("meta[itemprop=\"datePublished\"]", "content")
        }

        leadImageUrl {
            attr("meta[name=\"og:image\"]", "value")
            attr("meta[property=\"og:image\"]", "content")
            attr("meta[itemprop=\"image\"]", "content")
            attr("meta[name=\"twitter:image\"]", "content")
            attr("img.attachment-large", "src")
        }

        content {
            compound(
                ".elementor-section-wrap",
                ".elementor-text-editor > p, .elementor-text-editor > ul > li, .attachment-large, .wp-caption-text",
            )

            transform("figcaption", renameTo = "p")

            clean(".comments-area")
        }
    }
