package com.jocmp.mercury.resource.utils.dom

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.utils.dom.getAttrs
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

// Convert all instances of images with potentially
// lazy loaded images into normal images.
// Many sites will have img tags with no source, or an image tag with a src
// attribute that a is a placeholer. We need to be able to properly fill in
// the src attribute so the images are no longer lazy loaded.
fun convertLazyLoadedImages(doc: Doc): Doc {
    doc("img").each { _, img ->
        val attrs = getAttrs(img)
        attrs.keys.forEach { attr ->
            val value = attrs[attr] ?: return@forEach
            if (attr != "srcset" && IS_LINK.containsMatchIn(value) && IS_SRCSET.containsMatchIn(value)) {
                img.attr("srcset", value)
            } else if (
                attr != "src" &&
                attr != "srcset" &&
                IS_LINK.containsMatchIn(value) &&
                IS_IMAGE.containsMatchIn(value)
            ) {
                // Is the value a JSON object? If so, we should attempt to extract the image src from the data.
                val existingSrc = extractSrcFromJSON(value)
                if (existingSrc != null) {
                    img.attr("src", existingSrc)
                } else {
                    img.attr("src", value)
                }
            }
        }
    }
    return doc
}

private val JSON =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

private fun extractSrcFromJSON(value: String): String? {
    return try {
        val src = JSON.parseToJsonElement(value).jsonObject["src"] as? JsonPrimitive ?: return null
        if (src.isString) src.content else null
    } catch (_: Throwable) {
        null
    }
}
