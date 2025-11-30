package com.jocmp.capy.articles

import com.jocmp.capy.Article
import org.json.JSONObject

fun parseHtml(
    article: Article,
    hideImages: Boolean
): String {
    val json = JSONObject(
        mapOf(
            "url" to article.url?.toString(),
            "html" to article.content,
            "hideImages" to hideImages,
        )
    )

    return """
      <script>
        (async () => {
          const input = $json;
          displayFullContent(input);
        })();
      </script>
    """.trimIndent()
}
