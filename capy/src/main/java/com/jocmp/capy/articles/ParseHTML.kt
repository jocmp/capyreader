package com.jocmp.capy.articles

import com.jocmp.capy.Article
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun parseHtml(
    article: Article,
    hideImages: Boolean
): String {
    val json = buildJsonObject {
        put("url", article.url?.toString())
        put("html", article.content)
        put("hideImages", hideImages)
    }

    return """
      <script>
        (async () => {
          const input = ${scriptSafeJson(json)};
          displayFullContent(input);
        })();
      </script>
    """.trimIndent()
}

private fun scriptSafeJson(json: JsonObject): String {
    return json.toString().replace("</", "<\\/")
}

fun postProcessScript(article: Article, hideImages: Boolean): String {
    val baseUrl = article.url?.toString() ?: article.siteURL ?: ""

    return """
      <script>
        (function() {
          postProcessContent("$baseUrl", $hideImages);
        })();
      </script>
    """.trimIndent()
}
