package com.jocmp.mercury.extractors.custom.news.pts.org.tw

import com.jocmp.mercury.extractors.extractor

val NewsPtsOrgTwExtractor =
    extractor("news.pts.org.tw") {
        title { selectors("h1.article-title") }

        author {
            attr("meta[name=\"author\"]", "content")
            attr("meta[name=\"author\"]", "value")
        }

        datePublished {
            attr("meta[property=\"article:published_time\"]", "content")
            attr("meta[name=\"article:published_time\"]", "value")
        }

        dek {
            attr("meta[name=\"description\"]", "content")
            attr("meta[name=\"description\"]", "value")
        }

        leadImageUrl {
            attr("meta[property=\"og:image\"]", "content")
            attr("meta[name=\"og:image\"]", "value")
        }

        content {
            selectors(".post-article", ".article-content")

            clean(".articleimg", "ul")
        }
    }
