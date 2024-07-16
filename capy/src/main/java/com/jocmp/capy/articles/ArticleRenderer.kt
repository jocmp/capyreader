package com.jocmp.capy.articles

import android.content.Context
import com.jocmp.capy.Article
import com.jocmp.capy.MacroProcessor
import com.jocmp.capy.common.toDeviceDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.jocmp.capy.R as CapyRes

class ArticleRenderer(
    private val context: Context,
    private val colors: Map<String, String>
) {
    private val styles by lazy {
        context.resources.openRawResource(CapyRes.raw.stylesheet)
            .bufferedReader()
            .readText()
    }

    private val template by lazy {
        context.resources.openRawResource(CapyRes.raw.template)
            .bufferedReader()
            .readText()
    }

    fun render(
        article: Article,
        extractedContent: ExtractedContent = ExtractedContent(),
    ): String {
        val substitutions = colors + mapOf(
            "external_link" to article.url.toString(),
            "title" to article.title,
            "byline" to byline(article),
            "feed_name" to article.feedName,
            "body" to body(article, extractedContent),
            "style" to styles,
            "script" to script(article, extractedContent)
        )

        return MacroProcessor(
            template = template,
            substitutions = substitutions
        ).renderedText
    }

    private fun script(article: Article, extractedContent: ExtractedContent): String {
        val content = extractedContent.value()

        if (extractedContent.requestShow && content != null) {
            return context.extractedTemplate(article, content)
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

    private fun byline(article: Article): String {
        val deviceDateTime = article.publishedAt.toDeviceDateTime()
        val date = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(deviceDateTime)
        val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(deviceDateTime)
        val articleAuthor = article.author

        return if (!articleAuthor.isNullOrBlank()) {
            context.getString(CapyRes.string.article_byline, date, time, articleAuthor)
        } else {
            context.getString(CapyRes.string.article_byline_date_only, date, time)
        }
    }
}
