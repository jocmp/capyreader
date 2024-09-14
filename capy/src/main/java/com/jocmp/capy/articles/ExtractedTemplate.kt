package com.jocmp.capy.articles

import com.jocmp.capy.Article
import net.dankito.readability4j.Readability4J
import org.json.JSONObject
import org.jsoup.Jsoup


fun extractedTemplate(
    article: Article,
    html: String,
): String {
    if (article.extractedContentURL?.toString() != null) {
        return """
          <script>
            (() => {
              let { html } = ${JSONObject(mapOf("html" to html))};            

              $swapContentScript
            })();
          </script>
        """.trimIndent()
    }

    val parsed = parseHtml(article, html)

    return """
      <script>
        (() => {
          let { html } = ${JSONObject(mapOf("html" to parsed))};

          $swapContentScript
        })();
      </script>
    """.trimIndent()
}

private fun parseHtml(article: Article, html: String): String {
    try {
        val uri = (article.feedURL ?: article.url).toString()
        val readability4J = Readability4J(uri, html)
        val content = readability4J.parse().content ?: return ""

        val document = Jsoup.parse(content)

        document.getElementsByClass("readability-styled").forEach { element ->
            element.append("&nbsp;")
        }

        return document.body().html()
    } catch (ex: Throwable) {
        return ""
    }
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
