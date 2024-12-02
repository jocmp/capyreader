package com.jocmp.capy.articles

import com.jocmp.capy.Article
import org.json.JSONObject
import org.jsoup.nodes.Document

fun parseHtml(article: Article, document: Document): String {
    val html = document.html()

    return """
      <script>
        (async () => {
          let downloaded = ${JSONObject(mapOf("value" to html))};

          Mercury.parse("${article.url?.toString()}", { html: downloaded.value }).then(article => {
            let extracted = document.createElement("div");

            extracted.id = "article-body-content"
            extracted.innerHTML = article.content;

            let shouldAddImage = article.lead_image_url &&
                ![...extracted.querySelectorAll("img")].some(img => img.src.includes(article.lead_image_url));

            if (shouldAddImage) {
              let leadImage = document.createElement("img");
              leadImage.src = article.lead_image_url;
              extracted.prepend(leadImage);
            }

            let content = document.getElementById("article-body-content");
            content.replaceWith(extracted);
          });
        })();
      </script>
    """.trimIndent()
}
