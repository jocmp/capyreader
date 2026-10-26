package com.jocmp.mercury.extractors.custom.twitter.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor
import org.jsoup.nodes.Element

val TwitterComExtractor =
    extractor("twitter.com") {
        author { selectors(".tweet.permalink-tweet .username") }

        datePublished { attr(".permalink-tweet ._timestamp[data-time-ms]", "data-time-ms") }

        content {
            selectors(".permalink[role=main]")

            defaultCleaner = false

            // We're transforming essentially the whole page here. Twitter
            // doesn't have nice selectors, so our initial selector grabs the
            // whole page, then we're re-writing it to fit our needs before
            // we clean it up.
            transform(".permalink[role=main]") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val tweets = el.select(".tweet").map { it as Element }
                val container = Element("div").attr("id", "TWEETS_GO_HERE")
                tweets.forEach { container.appendChild(it) }
                el.replaceWith(container)
                TransformResult.NoChange
            }

            // Twitter wraps @ with s, which renders as a strikethrough.
            transform("s", renameTo = "span")

            clean(".stream-item-footer", "button", ".tweet-details-fixer")
        }
    }
