package com.jocmp.capy.articles

import android.content.Context
import com.jocmp.capy.Article
import com.jocmp.capy.MacroProcessor
import com.jocmp.capy.common.toDeviceDateTime
import com.jocmp.capy.preferences.Preference
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.jocmp.capy.R as CapyRes

class ArticleRenderer(
    private val context: Context,
    private val textSize: Preference<TextSize>,
    private val fontOption: Preference<FontOption>,
) {
    private var html: String = ""

    private val template by lazy {
        context.resources.openRawResource(CapyRes.raw.template)
            .bufferedReader()
            .readText()
    }

    fun render(
        article: Article,
        byline: String,
        extractedContent: ExtractedContent = ExtractedContent(),
        colors: Map<String, String>,
    ): String {
        val substitutions = colors + mapOf(
            "external_link" to article.url.toString(),
            "title" to article.title,
            "byline" to byline,
            "feed_name" to article.feedName,
            "body" to body(article, extractedContent),
            "script" to script(article, extractedContent),
            "text_size" to textSize.get().slug,
            "font_family" to fontOption.get().slug,
        )

        html = MacroProcessor(
            template = template,
            substitutions = substitutions
        ).renderedText

        val document = Jsoup.parse(html).apply {
            article.feedURL?.let { setBaseUri(it) }
        }

        cleanLinks(document)

        return document.html()
    }

    private fun cleanLinks(document: Document) {
        document.getElementsByTag("img").forEach { element ->
            element.attr("loading", "lazy")
        }

        document.select("img[data-src]").forEach { element ->
            element.attr("src", element.absUrl("data-src"))
        }
    }

    fun clear() {
        html = ""
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
