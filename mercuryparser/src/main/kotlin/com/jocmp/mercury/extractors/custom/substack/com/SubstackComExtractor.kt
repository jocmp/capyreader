package com.jocmp.mercury.extractors.custom.substack.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.nodes.Element

val SubstackComExtractor =
    extractor("substack.com") {
        title { attr("meta[name=\"og:title\"]", "value") }

        author { attr("meta[name=\"author\"]", "value") }

        datePublished { attr(".post-header time[datetime]", "datetime") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors(".available-content")

            // Lightbox images sit in an a.image-link, the rest in a
            // div.image-link that the generic cleaner drops for its "link" weight.
            transform(".image-link") { node, _ ->
                val link = node.get(0) ?: return@transform TransformResult.NoChange
                val image = link.selectFirst("picture") ?: link.selectFirst("img")

                if (image == null) {
                    link.remove()
                } else {
                    link.replaceWith(image)
                }

                TransformResult.NoChange
            }

            // "footnote" is a negative class hint, so the notes would be cleaned away.
            transform("div.footnote, div.footnote-content") { node, _ ->
                node.removeAttr("class")
                TransformResult.NoChange
            }

            transform("a[data-component-name=\"Twitter2ToDOM\"]") { node, _ ->
                val embed = node.get(0) ?: return@transform TransformResult.NoChange
                val tweet = embed.selectFirst(".twitter-embed")?.attr("data-attrs")?.let(::parseTweet)

                if (tweet == null) {
                    embed.remove()
                } else {
                    embed.replaceWith(tweetQuote(tweet))
                }

                TransformResult.NoChange
            }

            clean(
                ".header-anchor-parent",
                ".image-link-expand",
                ".subscribe-widget",
                ".subscription-widget-wrap",
                ".subscription-widget-wrap-editor",
                ".button-wrapper",
                ".poll-embed",
                ".share-dialog",
            )
        }
    }

private fun parseTweet(attrs: String): JsonObject? = runCatching { Json.parseToJsonElement(attrs).jsonObject }.getOrNull()

private fun tweetQuote(tweet: JsonObject): Element {
    val quote = Element("blockquote")

    quote.appendElement("p").text(tweet.string("full_text"))

    tweet["quoted_tweet"]?.let { quoted ->
        val nested = quote.appendElement("blockquote")
        nested.appendElement("p").text(quoted.jsonObject.string("full_text"))
        nested.appendElement("p").text("— ${byline(quoted.jsonObject)}")
    }

    quote
        .appendElement("p")
        .appendText("— ")
        .appendElement("a")
        .attr("href", tweet.string("url"))
        .text(byline(tweet))

    return quote
}

private fun byline(tweet: JsonObject): String = "${tweet.string("name")} (@${tweet.string("username")})"

private fun JsonObject.string(key: String): String = this[key]?.jsonPrimitive?.contentOrNull.orEmpty()
