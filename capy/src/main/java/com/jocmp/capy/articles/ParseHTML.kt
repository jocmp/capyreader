package com.jocmp.capy.articles

import com.jocmp.capy.Article
import org.json.JSONObject
import org.jsoup.nodes.Document

fun parseHtml(
    article: Article,
    document: Document,
    hideImages: Boolean,
    fullContentParser: FullContentParserType
): String {
    val html = document.html()

    return """
      <script>
        (async () => {
          const input = ${
        JSONObject(
            mapOf(
                "url" to article.url?.toString(),
                "html" to html,
                "hideImages" to hideImages,
                "parserType" to fullContentParser.toString(),
            )
        )
    };

          displayFullContent(input);
        })();
      </script>
    """.trimIndent()
}
