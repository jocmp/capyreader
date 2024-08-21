package com.jocmp.capy.articles

import com.jocmp.capy.Article
import net.dankito.readability4j.Readability4J
import org.json.JSONObject


internal fun extractedTemplate(
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

    val readability4J = Readability4J(article.url.toString(), html)
    val content = readability4J.parse().content

    return """
      <script>
        (() => {
          let {html} = ${JSONObject(mapOf("html" to content))};

//          Mercury.parse("${article.url}", { html: downloaded.value }).then(({ content: html }) => {
              $swapContentScript
//          });
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
    debugger;
    content.replaceWith(extracted);
"""
