package com.jocmp.mercury.extractors.custom.www.apartmenttherapy.com

import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.nodes.Element

val ApartmentTherapyExtractor =
    extractor("www.apartmenttherapy.com") {
        title { selectors("h1.headline") }

        author { selectors(".PostByline__name") }

        datePublished { attr(".PostByline__timestamp[datetime]", "datetime") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.post__content")

            transform("div[data-render-react-id=\"images/LazyPicture\"]") { node, _ ->
                val el = node.elements.firstOrNull() ?: return@transform TransformResult.NoChange
                val data = el.attr("data-props")
                if (data.isNotEmpty()) {
                    runCatching {
                        val parsed = Json.parseToJsonElement(data).jsonObject
                        val src = parsed["sources"]!!.jsonArray[0].jsonObject["src"]!!.jsonPrimitive.content
                        val img = Element("img").attr("src", src)
                        el.replaceWith(img)
                    }
                }
                TransformResult.NoChange
            }
        }
    }
