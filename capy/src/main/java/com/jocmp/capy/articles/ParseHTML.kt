package com.jocmp.capy.articles

import com.jocmp.capy.Article
import net.dankito.readability4j.Readability4J

fun parseHtml(article: Article, html: String): String {
    try {
        val uri = (article.feedURL ?: article.url).toString()
        val readability4J = Readability4J(uri, html)
        val content = readability4J.parse().articleContent ?: return ""

        content.getElementsByClass("readability-styled").forEach { element ->
            element.append("&nbsp;")
        }

        return content.html()
    } catch (ex: Throwable) {
        return ""
    }
}
