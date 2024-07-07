package com.capyreader.app.ui.articles.detail

import android.content.Context
import com.jocmp.capy.Article
import com.jocmp.capy.MacroProcessor
import com.capyreader.app.R
import com.capyreader.app.common.toDeviceDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.jocmp.capy.R as BasilResource

class ArticleRenderer(
    private val context: Context,
    private val article: Article,
    private val extractedContent: ExtractedContent,
    private val template: String,
    private val styles: String,
    private val colors: Map<String, String>
) {
    fun render(): String {
        val substitutions = colors + mapOf(
            "external_link" to article.url.toString(),
            "title" to article.title,
            "byline" to byline,
            "feed_name" to article.feedName,
            "body" to body(),
            "style" to styles
        )

        return MacroProcessor(
            template = template,
            substitutions = substitutions
        ).renderedText
    }

    private fun body(): String {
        val content = extractedContent.value()

        if (extractedContent.requestShow && content != null) {
            return content
        }

        return article.contentHTML.ifBlank {
            article.summary
        }
    }

    private val byline: String
        get() {
            val deviceDateTime = article.publishedAt.toDeviceDateTime()
            val date = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(deviceDateTime)
            val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(deviceDateTime)
            val articleAuthor = article.author

            return if (!articleAuthor.isNullOrBlank()) {
                context.getString(R.string.article_byline, date, time, articleAuthor)
            } else {
                context.getString(R.string.article_byline_date_only, date, time)
            }
        }

    companion object {
        fun render(
            article: Article,
            templateColors: TemplateColors,
            context: Context,
            extractedContent: ExtractedContent = ExtractedContent(),
        ): String {
            return context.resources.openRawResource(BasilResource.raw.stylesheet)
                .use { styleStream ->
                    context.resources.openRawResource(BasilResource.raw.template)
                        .use { templateStream ->
                            val template = templateStream.bufferedReader().readText()
                            val style = styleStream.bufferedReader().readText()

                            ArticleRenderer(
                                article = article,
                                extractedContent = extractedContent,
                                template = template,
                                styles = style,
                                colors = templateColors.asMap(),
                                context = context
                            ).render()
                        }
                }
        }
    }
}
