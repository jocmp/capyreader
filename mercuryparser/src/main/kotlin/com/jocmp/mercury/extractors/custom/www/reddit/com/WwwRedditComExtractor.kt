package com.jocmp.mercury.extractors.custom.www.reddit.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

private val BG_IMAGE_URL = Regex("""\((.*?)\)""")
private val BG_IMAGE_QUOTES = Regex("""['"]""")

val WwwRedditComExtractor =
    extractor("www.reddit.com") {
        title {
            selectors(
                "div[data-test-id=\"post-content\"] h1",
                "div[data-test-id=\"post-content\"] h2",
            )
        }

        author { selectors("div[data-test-id=\"post-content\"] a[href*=\"user/\"]") }

        datePublished {
            selectors(
                "div[data-test-id=\"post-content\"] span[data-click-id=\"timestamp\"]",
                "div[data-test-id=\"post-content\"] a[data-click-id=\"timestamp\"]",
            )
        }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            compound("div[data-test-id=\"post-content\"] p") // text post
            compound(
                "div[data-test-id=\"post-content\"] a[target=\"_blank\"]:not([data-click-id=\"timestamp\"])",
                "div[data-test-id=\"post-content\"] div[data-click-id=\"media\"]",
            ) // external link with media preview
            compound("div[data-test-id=\"post-content\"] div[data-click-id=\"media\"]") // embedded media
            compound("div[data-test-id=\"post-content\"] a") // external link
            selector("div[data-test-id=\"post-content\"]")

            transform("div[role=\"img\"]") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val img = el.selectFirst("img")
                val style = el.attr("style")
                val bgMatch = BG_IMAGE_URL.find(style)
                if (img != null && bgMatch != null) {
                    val url = bgMatch.groupValues[1].replace(BG_IMAGE_QUOTES, "")
                    img.attr("src", url)
                }
                TransformResult.NoChange
            }

            clean(
                ".icon",
                "span[id^=\"PostAwardBadges\"]",
                "div a[data-test-id=\"comments-page-link-num-comments\"]",
            )
        }
    }
