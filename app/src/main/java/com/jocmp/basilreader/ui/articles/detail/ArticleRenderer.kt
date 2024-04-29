package com.jocmp.basilreader.ui.articles.detail

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import com.jocmp.basil.Article
import com.jocmp.basil.MacroProcessor
import com.jocmp.basil.R as BasilResource
import com.jocmp.basilreader.R
import com.jocmp.basilreader.common.toDeviceDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ArticleRenderer(
    private val context: Context,
    private val article: Article,
    private val template: String,
    private val styles: String,
    private val colors: Map<String, String>
) {
    private val body = article.contentHTML.ifBlank {
        article.summary
    }

    fun render(): String {
        val substitutions = colors + mapOf(
            "external_link" to article.url.toString(),
            "title" to article.title,
            "byline" to byline,
            "feed_name" to article.feedName,
            "body" to body,
            "style" to styles
        )

        return MacroProcessor(
            template = template,
            substitutions = substitutions
        ).renderedText
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
        fun render(article: Article?, templateColors: TemplateColors, context: Context): String {
            if (article == null) {
                return ""
            }

            return context.resources.openRawResource(BasilResource.raw.stylesheet).use { styleStream ->
                context.resources.openRawResource(BasilResource.raw.template).use { templateStream ->
                    val template = templateStream.bufferedReader().readText()
                    val style = styleStream.bufferedReader().readText()

                    ArticleRenderer(
                        article = article,
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