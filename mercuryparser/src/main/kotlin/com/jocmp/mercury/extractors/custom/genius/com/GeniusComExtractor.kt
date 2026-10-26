package com.jocmp.mercury.extractors.custom.genius.com

import com.jocmp.mercury.extractors.extractor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val json = Json { ignoreUnknownKeys = true }

private fun parseSongField(
    raw: String,
    extract: (JsonObject) -> String?,
): String? =
    runCatching {
        val song = json.parseToJsonElement(raw).jsonObject["song"]?.jsonObject ?: return@runCatching null
        extract(song)
    }.getOrNull()

val GeniusComExtractor =
    extractor("genius.com") {
        title { selectors("h1") }

        author { selectors("h2 a") }

        datePublished {
            attr("meta[itemprop=page_data]", "value") { raw ->
                parseSongField(raw) { it["release_date"]?.jsonPrimitive?.contentOrNull }
            }
        }

        leadImageUrl {
            attr("meta[itemprop=page_data]", "value") { raw ->
                parseSongField(raw) {
                    it["album"]?.jsonObject?.get("cover_art_url")?.jsonPrimitive?.contentOrNull
                }
            }
        }

        content {
            selectors(".lyrics")
        }
    }
