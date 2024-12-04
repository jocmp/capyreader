package com.jocmp.capy.articles

import com.jocmp.capy.Article
import org.json.JSONObject
import org.jsoup.nodes.Document

fun parseHtml(article: Article, document: Document, hideImages: Boolean): String {
    val html = document.html()

    return """
      <script>
        (async () => {
          let { html, hideImages } = ${JSONObject(mapOf("html" to html, "hideImages" to hideImages))};

          Mercury.parse("${article.url?.toString()}", { html }).then(article => {
            let extracted = document.createElement("div");

            extracted.id = "article-body-content"
            extracted.innerHTML = article.content;

            let shouldAddImage = article.lead_image_url &&
                !hideImages &&
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
