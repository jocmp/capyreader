package com.jocmp.capy.articles

import com.jocmp.capy.Article

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
