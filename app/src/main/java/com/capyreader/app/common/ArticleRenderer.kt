package com.capyreader.app.common

import android.content.Context
import com.capyreader.app.ui.articles.detail.byline
import com.jocmp.capy.Article
import com.jocmp.capy.MacroProcessor
import com.jocmp.capy.articles.ExtractedContent
import com.jocmp.capy.articles.FontOption
import com.jocmp.capy.articles.TextSize
import com.jocmp.capy.articles.extractedTemplate
import com.jocmp.capy.preferences.Preference
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import com.jocmp.capy.R as CapyRes

class ArticleRenderer(
    private val context: Context,
    private val textSize: Preference<TextSize>,
    private val fontOption: Preference<FontOption>,
) {
    private val template by lazy {
        context.resources.openRawResource(CapyRes.raw.template)
            .bufferedReader()
            .readText()
    }

    fun render(
        article: Article,
        extractedContent: ExtractedContent = ExtractedContent(),
        colors: Map<String, String>,
    ): String {
        val substitutions = colors + mapOf(
            "external_link" to article.url.toString(),
            "title" to article.title,
            "byline" to article.byline(context),
            "feed_name" to article.feedName,
            "body" to body(article, extractedContent),
            "script" to script(article, extractedContent),
            "text_size" to textSize.get().slug,
            "font_family" to fontOption.get().slug,
        )

        val html = MacroProcessor(
            template = template,
            substitutions = substitutions
        ).renderedText

        val document = Jsoup.parse(html).apply {
            article.siteURL?.let { setBaseUri(it) }
        }

        cleanLinks(document)

        return document.html()
    }

    private fun cleanLinks(document: Document) {
        document.getElementsByTag("img").forEach { element ->
            element.attr("loading", "lazy")
            element.attr("src", element.absUrl("src"))
        }

        document.select("img[data-src]").forEach { element ->
            element.attr("src", element.absUrl("data-src"))
        }
    }

    private fun script(article: Article, extractedContent: ExtractedContent): String {
        val content = extractedContent.value()

        if (extractedContent.requestShow && content != null) {
            return extractedTemplate(article, content)
        }

        return ""
    }

    private fun body(article: Article, extractedContent: ExtractedContent): String {
        if (extractedContent.showByDefault) {
            return ""
        }

        return article.contentHTML.ifBlank {
            article.summary
        }
    }
}
