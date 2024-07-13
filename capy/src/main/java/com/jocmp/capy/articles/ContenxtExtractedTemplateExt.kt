package com.jocmp.capy.articles

import android.content.Context
import com.jocmp.capy.Article
import com.jocmp.capy.R
import org.json.JSONObject

internal fun Context.extractedTemplate(
    article: Article,
    html: String,
): String {
    if (article.extractedContentURL?.toString() != null) {
        return """
          <script>
            (() => {
              let downloaded = ${JSONObject(mapOf("value" to html))};
              let html = downloaded.value;

              $swapContentScript
            })();
          </script>
        """.trimIndent()
    }

    val mercury = resources.openRawResource(R.raw.mercury)
        .bufferedReader()
        .readText()

    return """
      <script>
      $mercury
      </script>
      <script>
        (() => {
          let downloaded = ${JSONObject(mapOf("value" to html))};

          Mercury.parse("${article.url}", { html: downloaded.value }).then(({ content: html }) => {
            $swapContentScript
          });
        })();
      </script>
    """.trimIndent()
}

// Depends on the presence of a variable named "html"
const val swapContentScript = """
    let extracted = document.createElement("div");    
    extracted.id = "article-body-content"
    extracted.innerHTML = html;

    let content = document.getElementById("article-body-content");
    content.replaceWith(extracted);
"""
