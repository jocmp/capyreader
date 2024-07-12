package com.capyreader.app.ui.articles.detail

import android.content.Context
import com.capyreader.app.R
import com.capyreader.app.common.toDeviceDateTime
import com.jocmp.capy.Article
import com.jocmp.capy.MacroProcessor
import org.json.JSONObject
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.jocmp.capy.R as CapyRes

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
            return decoratedContent(content)
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

    private fun decoratedContent(content: String): String {
        if (article.extractedContentURL?.toString() == null) {
            val mercury = context.resources.openRawResource(CapyRes.raw.mercury)
                .bufferedReader()
                .readText()

            return """
              <script>
              $mercury
              </script>
              <script>
                var html = ${JSONObject(mapOf("value" to content))};

                Mercury.parse("${article.url}", { html: html.value }).then(({ content }) => {
                  document.getElementById("article-body").insertAdjacentHTML("beforeend", content);
                });
              </script>
            """.trimIndent()
        } else {
            return content
        }
    }

    companion object {
        fun render(
            article: Article,
            templateColors: TemplateColors,
            context: Context,
            extractedContent: ExtractedContent = ExtractedContent(),
        ): String {
            val style = context.resources.openRawResource(CapyRes.raw.stylesheet)
                .bufferedReader()
                .readText()

            val template = context.resources.openRawResource(CapyRes.raw.template)
                .bufferedReader()
                .readText()

            return ArticleRenderer(
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
